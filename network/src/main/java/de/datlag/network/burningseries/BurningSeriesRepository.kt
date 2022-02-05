package de.datlag.network.burningseries

import android.util.Log
import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkBoundResource
import de.datlag.database.burningseries.BurningSeriesDao
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.allseries.relation.GenreWithItems
import de.datlag.model.burningseries.allseries.search.GenreItemWithMatchInfo
import de.datlag.model.burningseries.home.HomeData
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.series.EpisodeInfo
import de.datlag.model.burningseries.series.SeasonData
import de.datlag.model.burningseries.series.SeriesData
import de.datlag.model.burningseries.series.relation.SeriesLanguagesCrossRef
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.model.common.calculateScore
import de.datlag.network.common.toInt
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Named

@Obfuscate
class BurningSeriesRepository @Inject constructor(
	private val service: BurningSeries,
	@Named("wrapApiToken") val wrapApiToken: String,
	private val burningSeriesDao: BurningSeriesDao,
	private val scraper: BurningSeriesScraper
) {

	fun getHomeData(): Flow<Resource<HomeData>> = flow {
		val firstOrNullEpisodes = burningSeriesDao.getAllLatestEpisode().firstOrNull()
		val firstOrNullSeries = burningSeriesDao.getAllLatestSeries().firstOrNull()
		emit(Resource.loading(if (firstOrNullEpisodes.isNullOrEmpty() || firstOrNullSeries.isNullOrEmpty()) {
			null
		} else {
			HomeData(firstOrNullEpisodes, firstOrNullSeries)
		}))
		val scrapeData = scraper.scrapeHomeData()

		if (scrapeData != null) {
			saveHomeData(scrapeData)
			emit(Resource.success(scrapeData))
		} else {
			val currentRequest = Clock.System.now().epochSeconds
			networkBoundResource(
				fetchFromLocal = {
					flow<Pair<List<LatestEpisode>, List<LatestSeries>>> {
						val emitEpisodes: MutableList<LatestEpisode> = burningSeriesDao.getAllLatestEpisode().first().toMutableList()
						val emitSeries: MutableList<LatestSeries> = burningSeriesDao.getAllLatestSeries().first().toMutableList()
						emit(Pair(emitEpisodes, emitSeries))

						burningSeriesDao.getAllLatestEpisode().collect {
							if (!emitEpisodes.containsAll(it)) {
								emitEpisodes.clear()
								emitEpisodes.addAll(it)
								if (emitEpisodes.isNotEmpty() && emitSeries.isNotEmpty()) {
									emit(Pair(emitEpisodes, emitSeries))
								}
							}
						}
						burningSeriesDao.getAllLatestSeries().collect {
							if (!emitSeries.containsAll(it)) {
								emitSeries.clear()
								emitSeries.addAll(it)
								if (emitEpisodes.isNotEmpty() && emitSeries.isNotEmpty()) {
									emit(Pair(emitEpisodes, emitSeries))
								}
							}
						}
					}.flowOn(Dispatchers.IO)
				},
				shouldFetchFromRemote = {
					it == null || it.first.isNullOrEmpty() || it.second.isNullOrEmpty() || it.first.any { episode ->
						(currentRequest - Constants.DAY_IN_MILLI) >= episode.updatedAt
					} || it.second.any { series ->
						(currentRequest - Constants.DAY_IN_MILLI) >= series.updatedAt
					}
				},
				fetchFromRemote = {
					service.getHomeData(apiKey = wrapApiToken)
				},
				saveRemoteData = {
					if (it.success && it.data.latestEpisodes.isNotEmpty() && it.data.latestSeries.isNotEmpty()) {
						saveHomeData(it.data)
					}
				}
			).collect {
				when (it.status) {
					Resource.Status.LOADING -> {
						if (it.data != null) {
							emit(Resource.loading(HomeData(it.data!!.first, it.data!!.second)))
						}
					}
					Resource.Status.SUCCESS -> {
						if (it.data != null) {
							emit(Resource.success(HomeData(it.data!!.first, it.data!!.second)))
						}
					}
					Resource.Status.ERROR -> emit(Resource.error<HomeData>(it.message ?: String()))
				}
			}
		}
	}.flowOn(Dispatchers.IO)

	private suspend fun saveHomeData(home: HomeData) {
		burningSeriesDao.deleteAllLatestEpisode()
		burningSeriesDao.deleteAllLatestSeries()

		home.latestEpisodes.forEach {
			burningSeriesDao.insertLatestEpisode(it)
		}
		home.latestSeries.forEach {
			burningSeriesDao.insertLatestSeries(it)
		}
	}

	fun getSeriesData(latestSeries: LatestSeries) = getSeriesData(latestSeries.href, latestSeries.getHrefTitle())

	fun getSeriesData(latestEpisode: LatestEpisode) = getSeriesData(latestEpisode.getHrefWithoutEpisode(), latestEpisode.getHrefTitle())

	fun getSeriesData(genreItem: GenreModel.GenreItem) = getSeriesData(genreItem.href, genreItem.getHrefTitle())

	fun getSeriesData(href: String, hrefTitle: String, forceLoad: Boolean = false): Flow<Resource<SeriesWithInfo?>> = flow {
		val hrefData = hrefDataFromHref(href)
		emit(Resource.loading(burningSeriesDao.getSeriesWithInfoBestMatch(hrefTitle).firstOrNull()))
		val scrapeData = scraper.scrapeSeriesData(rebuildHrefFromData(hrefData))

		if (scrapeData != null) {
			saveSeriesData(scrapeData)
			emitAll(burningSeriesDao.getSeriesWithInfoBestMatch(hrefTitle).map { Resource.success(it) })
		} else {
			val currentRequest = Clock.System.now().epochSeconds
			emitAll(networkBoundResource(
				fetchFromLocal = {
					burningSeriesDao.getSeriesWithInfoBestMatch(hrefTitle)
				},
				shouldFetchFromRemote = {
					it == null || forceLoad || (currentRequest - Constants.DAY_IN_MILLI) >= it.series.updatedAt || it.episodes.isEmpty()
				},
				fetchFromRemote = {
					if (hrefData.second != null && hrefData.third != null) {
						service.getSeriesData(
							apiKey = wrapApiToken,
							series = hrefData.first,
							season = hrefData.second!!,
							language = hrefData.third!!
						)
					} else if (hrefData.second != null && hrefData.third == null) {
						service.getSeriesData(
							apiKey = wrapApiToken,
							series = hrefData.first,
							season = hrefData.second!!
						)
					} else {
						service.getSeriesData(
							apiKey = wrapApiToken,
							series = hrefData.first
						)
					}
				},
				saveRemoteData = { series ->
					if (series.success) {
						series.data?.let {
							saveSeriesData(it.apply {
								this.href = href
							})
						}
					}
				}
			))
		}
	}.flowOn(Dispatchers.IO)

	private fun hrefDataFromHref(href: String): Triple<String, String?, String?> {
		var newHref = href
		if (newHref.startsWith('/')) {
			newHref = newHref.substring(1)
		}
		val hrefSplit = newHref.split('/')
		val season = if (hrefSplit.size >= 3) hrefSplit[2] else null
		val language = if (hrefSplit.size >= 4) hrefSplit[3] else null
		val fallbackLanguage = if (hrefSplit.size >= 5) hrefSplit[4] else null
		return Triple(
			hrefSplit[1],
			if (season.isNullOrEmpty()) null else season,
			if (fallbackLanguage != null && fallbackLanguage.isNotEmpty()) {
				fallbackLanguage
			} else {
				if (language.isNullOrEmpty()) null else language
			}
		)
	}

	private fun rebuildHrefFromData(hrefData: Triple<String, String?, String?>): String {
		return if (hrefData.second != null && hrefData.third != null) {
			"serie/${hrefData.first}/${hrefData.second}/${hrefData.third}"
		} else if (hrefData.second != null && hrefData.third == null) {
			"serie/${hrefData.first}/${hrefData.second}"
		} else if (hrefData.second == null && hrefData.third != null) {
			"serie/${hrefData.first}/${hrefData.third}"
		} else {
			"serie/${hrefData.first}"
		}
	}

	private suspend fun saveSeriesData(seriesData: SeriesData) {
		val previousSeries = burningSeriesDao.getSeriesWithEpisodesBestMatch(seriesData.hrefTitle).firstOrNull()

		val favSince = previousSeries?.series?.favoriteSince ?: 0L
		val watchProgress: Map<String, Pair<Long, Long>> = previousSeries?.episodes?.map { it.href to (it.currentWatchPos to it.totalWatchPos) }?.toMap() ?: mapOf()

		seriesData.favoriteSince = favSince

		burningSeriesDao.getEpisodeWithHoster()

		val seriesId = burningSeriesDao.insertSeriesData(seriesData)
		seriesData.infos.forEach {
			it.seriesId = seriesId
			burningSeriesDao.insertInfoData(it)
		}
		seriesData.seasons.forEach {
			burningSeriesDao.insertSeasonData(it.apply { this.seriesId = seriesId })
		}
		seriesData.languages.forEach {
			val langId = burningSeriesDao.addLanguageData(it)
			burningSeriesDao.insertSeriesLanguagesCrossRef(SeriesLanguagesCrossRef(seriesId, langId))
		}
		seriesData.episodes.forEach { episode ->
			episode.seriesId = seriesId
			val episodeWatchProgress = if (watchProgress.containsKey(episode.href)) {
				watchProgress.getOrElse(episode.href) { 0L to 0L }
			} else {
				0L to 0L
			}
			episode.currentWatchPos = episodeWatchProgress.first
			episode.totalWatchPos = episodeWatchProgress.second
			val episodeId = burningSeriesDao.insertEpisodeInfo(episode)
			episode.hoster.forEach {
				it.episodeId = episodeId
				burningSeriesDao.insertHoster(it)
			}
		}
	}

	suspend fun updateSeriesFavorite(seriesData: SeriesData) = burningSeriesDao.updateSeriesFavorite(seriesData.seriesId, seriesData.favoriteSince)

	suspend fun updateEpisodeInfo(episodeInfo: EpisodeInfo) = burningSeriesDao.updateEpisodeInfo(episodeInfo)

	fun getSeriesFavorites(): Flow<List<SeriesWithInfo>> = burningSeriesDao.getSeriesFavorites().flowOn(Dispatchers.IO)

	fun searchSeriesFavorites(title: String): Flow<List<SeriesWithInfo>> = burningSeriesDao.searchFavorites(title).flowOn(Dispatchers.IO)

	fun getAllSeries(pagination: Long): Flow<Resource<List<GenreWithItems>>> = flow {
		if (pagination == 0) {
			val first = burningSeriesDao.getAllSeries(pagination).first()
			val currentRequest = Clock.System.now().epochSeconds

			if (first.isEmpty() || first.any { (currentRequest - Constants.DAY_IN_MILLI) >= it.genre.updatedAt }) {
				emit(Resource.loading(first))
				val scrapeData = scraper.scrapeAllSeries()
				if (scrapeData.isNotEmpty()) {
					saveGenreData(scrapeData)
					emitAll(burningSeriesDao.getAllSeries().map { Resource.success(it) })
				} else {
					emitAll(networkBoundResource(
						fetchFromLocal = {
							burningSeriesDao.getAllSeries(pagination)
						},
						shouldFetchFromRemote = {
							it.isNullOrEmpty() || it.any { item -> (currentRequest - Constants.DAY_IN_MILLI) >= item.genre.updatedAt }
						},
						fetchFromRemote = {
							service.getAllSeries(apiKey = wrapApiToken)
						},
						saveRemoteData = { all ->
							if (all.success) {
								saveGenreData(all.data)
							}
						}
					))
				}
			} else {
				emit(Resource.success(first))
			}
		} else {
			emit(Resource.success(burningSeriesDao.getAllSeries(pagination).first()))
		}
	}.flowOn(Dispatchers.IO)

	fun getAllSeriesCount() = burningSeriesDao.getAllSeriesCount().flowOn(Dispatchers.IO)

	fun searchAllSeries(title: String): Flow<List<GenreModel>> = flow<List<GenreModel>> {
		emitAll(burningSeriesDao.searchAllSeries(escapeSearchQuery(title)).map {
			it.sortedWith(compareByDescending<GenreItemWithMatchInfo> { item ->
				item.genreItem.title.equals(title, true).toInt()
			}.thenByDescending { item ->
				item.genreItem.title.startsWith(title, true).toInt()
			}.thenByDescending { item ->
				item.genreItem.title.contains(title, true).toInt()
			}.thenByDescending { item -> item.matchInfo.calculateScore() }).map { item ->
				item.genreItem
			}
		})
	}.flowOn(Dispatchers.IO)

	private fun escapeSearchQuery(query: String): String {
		val queryWithEscapedQuotes = query.replace(Regex.fromLiteral("\""), "\"\"")
		return "\"$queryWithEscapedQuotes\""
	}

	private suspend fun saveGenreData(genreList: List<GenreModel.GenreData>) {
		genreList.sortedBy { it.genre }.forEach { genreData ->
			val genreId: Long = burningSeriesDao.insertGenre(genreData)
			genreData.items.forEach { item ->
				item.genreId = genreId
				burningSeriesDao.insertGenreItem(item)
			}
		}
	}
}