package de.datlag.network.burningseries

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkBoundResource
import com.hadiyarajesh.flower.networkResource
import de.datlag.database.burningseries.BurningSeriesDao
import de.datlag.model.Constants
import de.datlag.model.burningseries.Cover
import de.datlag.model.burningseries.allseries.GenreData
import de.datlag.model.burningseries.allseries.GenreItem
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.allseries.relation.GenreWithItems
import de.datlag.model.burningseries.allseries.search.GenreItemWithMatchInfo
import de.datlag.model.burningseries.home.HomeData
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.home.relation.*
import de.datlag.model.burningseries.series.*
import de.datlag.model.burningseries.series.relation.SeriesCoverCrossRef
import de.datlag.model.burningseries.series.relation.SeriesLanguagesCrossRef
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.model.burningseries.stream.Stream
import de.datlag.model.common.calculateScore
import de.datlag.model.video.ScrapeHoster
import de.datlag.network.common.toInt
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@Obfuscate
class BurningSeriesRepository @Inject constructor(
	private val service: BurningSeries,
	private val burningSeriesDao: BurningSeriesDao,
	@Named("coversDir") private val coversDir: File,
	val jsonBuilder: Json
) {

	fun getHomeData(): Flow<Resource<HomeData>> = flow {
		val firstOrNullEpisodes = burningSeriesDao.getAllLatestEpisode().firstOrNull()
		val firstOrNullSeries = burningSeriesDao.getAllLatestSeries().firstOrNull()
		emit(Resource.loading(if (firstOrNullEpisodes.isNullOrEmpty() || firstOrNullSeries.isNullOrEmpty()) {
			null
		} else {
			HomeData(
				mapLatestEpisodeWithInfoToLatestEpisode(firstOrNullEpisodes),
				mapLatestSeriesWithCoverToLatestSeries(firstOrNullSeries)
			)
		}))

		val currentRequest = Clock.System.now().epochSeconds
		networkBoundResource(
			fetchFromLocal = {
				flow<Pair<List<LatestEpisodeWithInfoFlags>, List<LatestSeriesWithCover>>> {
					val emitEpisodes: MutableList<LatestEpisodeWithInfoFlags> = burningSeriesDao.getAllLatestEpisode().first().sortedWith(
						compareBy<LatestEpisodeWithInfoFlags> { it.latestEpisode.title }.thenByDescending { it.latestEpisode.updatedAt }).toMutableList()
					val emitSeries: MutableList<LatestSeriesWithCover> = burningSeriesDao.getAllLatestSeries().first().sortedWith(
						compareBy<LatestSeriesWithCover> { it.latestSeries.title }.thenByDescending { it.latestSeries.updatedAt }
					).toMutableList()
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
				it == null || it.first.isEmpty() || it.second.isEmpty() || it.first.any { episode ->
					currentRequest - Constants.HOUR_IN_SECONDS >= episode.latestEpisode.updatedAt
				} || it.second.any { series ->
					currentRequest - Constants.HOUR_IN_SECONDS >= series.latestSeries.updatedAt
				}
			},
			fetchFromRemote = {
				service.getHomeData()
			},
			saveRemoteData = {
				if (it.latestEpisodes.isNotEmpty() && it.latestSeries.isNotEmpty()) {
					saveHomeData(it)
				}
			}
		).collect {
			when (it.status) {
				Resource.Status.LOADING -> {
					if (it.data != null) {
						emit(Resource.loading(HomeData(
							mapLatestEpisodeWithInfoToLatestEpisode(it.data!!.first),
							mapLatestSeriesWithCoverToLatestSeries(it.data!!.second)
						)))
					}
				}
				Resource.Status.SUCCESS -> {
					if (it.data != null) {
						emit(Resource.success(HomeData(
							mapLatestEpisodeWithInfoToLatestEpisode(it.data!!.first),
							mapLatestSeriesWithCoverToLatestSeries(it.data!!.second)
						)))
					}
				}
				is Resource.Status.ERROR -> {
					val errorStatus = it.status as Resource.Status.ERROR
					emit(Resource.error<HomeData>(errorStatus.message, errorStatus.statusCode))
				}
			}
		}
	}.flowOn(Dispatchers.IO)

	private fun mapLatestEpisodeWithInfoToLatestEpisode(list: List<LatestEpisodeWithInfoFlags>): List<LatestEpisode> {
		return list.map {
			LatestEpisode(
				it.latestEpisode.title,
				it.latestEpisode.href,
				it.latestEpisode.infoText,
				it.latestEpisode.updatedAt,
				it.latestEpisode.nsfw,
				if (it.cover?.isDefault() == true) it.latestEpisode.cover else it.cover ?: it.latestEpisode.cover,
				it.infoFlags
			).apply { latestEpisodeId = it.latestEpisode.latestEpisodeId }
		}.sortedWith(
			compareBy<LatestEpisode> { it.title }.thenByDescending { it.updatedAt }
		)
	}

	private fun mapLatestSeriesWithCoverToLatestSeries(list: List<LatestSeriesWithCover>): List<LatestSeries> {
		return list.map {
			LatestSeries(
				it.latestSeries.title,
				it.latestSeries.href,
				it.latestSeries.updatedAt,
				it.latestSeries.nsfw,
				if (it.cover?.isDefault() == true) it.latestSeries.cover else it.cover ?: it.latestSeries.cover
			).apply { latestSeriesId = it.latestSeries.latestSeriesId }
		}.sortedWith(
			compareBy<LatestSeries> { it.title }.thenByDescending { it.updatedAt }
		)
	}

	private suspend fun saveHomeData(home: HomeData) {
		burningSeriesDao.deleteAllLatestEpisode()
		burningSeriesDao.deleteAllLatestSeries()

		coroutineScope {
			home.latestEpisodes.map {
				async(Dispatchers.IO) {
					val latestEpisodeId = burningSeriesDao.insertLatestEpisode(it)
					val coverId = burningSeriesDao.addCover(it.cover, this@coroutineScope)
					burningSeriesDao.insertLatestEpisodeCoverCrossRef(
						LatestEpisodeCoverCrossRef(latestEpisodeId, coverId)
					)

					saveCover(it.cover)

					it.infoFlags.map { infoFlags ->
						async(Dispatchers.IO) {
							val latestEpisodeInfoFlagsId = burningSeriesDao.addLatestEpisodeInfoFlags(infoFlags, this@coroutineScope)
							burningSeriesDao.insertLatestEpisodeInfoFlagsCrossRef(
								LatestEpisodeInfoFlagsCrossRef(latestEpisodeId, latestEpisodeInfoFlagsId)
							)
						}
					}.awaitAll()
				}
			}.awaitAll()
			home.latestSeries.map {
				async(Dispatchers.IO) {
					val latestSeriesId = burningSeriesDao.insertLatestSeries(it)
					val coverId = burningSeriesDao.addCover(it.cover, this@coroutineScope)
					burningSeriesDao.insertLatestSeriesCoverCrossRef(
						LatestSeriesCoverCrossRef(latestSeriesId, coverId)
					)

					saveCover(it.cover)
				}
			}.awaitAll()
		}
	}

	private suspend fun saveCover(cover: Cover) = withContext(Dispatchers.IO) {
		if (cover.href.isNotEmpty() && cover.base64.isNotEmpty()) {
			if (!coversDir.exists()) {
				try {
					coversDir.mkdirs()
				} catch (ignored: Throwable) { }
			}
			val coverFile = File(coversDir, cover.href.substringAfterLast('/'))
			if (!coverFile.exists()) {
				try {
					coverFile.createNewFile()
				} catch (ignored: Throwable) { }
			}
			if (try {
			    coverFile.readText().isEmpty()
			} catch (ignored: Throwable) { true }) {
				coverFile.writeText(cover.base64)
			}
		}
	}

	fun getSeriesData(latestSeries: LatestSeries) = getSeriesData(latestSeries.href, latestSeries.getHrefTitle())

	fun getSeriesData(latestEpisode: LatestEpisode) = getSeriesData(latestEpisode.getHrefWithoutEpisode(), latestEpisode.getHrefTitle())

	fun getSeriesData(genreItem: GenreItem) = getSeriesData(genreItem.href, genreItem.getHrefTitle())

	fun getSeriesData(linkedSeries: LinkedSeriesData) = getSeriesData(linkedSeries.href, linkedSeries.getHrefTitle())

	fun getSeriesData(href: String, hrefTitle: String, forceLoad: Boolean = false): Flow<Resource<SeriesWithInfo?>> = flow {
		val currentRequest = Clock.System.now().epochSeconds
		emitAll(networkBoundResource(
			fetchFromLocal = {
				burningSeriesDao.getSeriesWithInfoBestMatch(hrefTitle)
			},
			shouldFetchFromRemote = {
				it == null || forceLoad || currentRequest - Constants.HOUR_IN_SECONDS >= it.series.updatedAt || it.episodes.isEmpty()
			},
			fetchFromRemote = {
				service.getSeriesData(href)
			},
			saveRemoteData = { series ->
				saveSeriesData(series.apply {
					this.href = href
				})
			}
		))
	}.flowOn(Dispatchers.IO)

	private suspend fun saveSeriesData(
		seriesData: SeriesData,
		infos: List<InfoData> = seriesData.infos,
		seasons: List<SeasonData> = seriesData.seasons,
		languages: List<LanguageData> = seriesData.languages,
		episodes: Map<EpisodeInfo, List<HosterData>> = seriesData.episodes.associateWith { it.hoster },
		linkedSeries: List<LinkedSeriesData> = seriesData.linkedSeries
	) = withContext(Dispatchers.IO) {
		val previousSeries = burningSeriesDao.getSeriesWithEpisodesBestMatch(seriesData.hrefTitle).firstOrNull()

		val favSince = previousSeries?.series?.favoriteSince ?: seriesData.favoriteSince
		val watchProgress: Map<String, Pair<Long, Long>> = previousSeries?.episodes?.associate {
			it.href to (it.currentWatchPos to it.totalWatchPos)
		} ?: mapOf()

		seriesData.favoriteSince = favSince

		val seriesId = burningSeriesDao.insertSeriesData(seriesData)
		val coverId = burningSeriesDao.addCover(seriesData.cover, this)
		burningSeriesDao.insertSeriesCoverCrossRef(
			SeriesCoverCrossRef(seriesId, coverId)
		)

		saveCover(seriesData.cover)

		coroutineScope {
			infos.map {
				async(Dispatchers.IO) {
					it.seriesId = seriesId
					burningSeriesDao.insertInfoData(it)
				}
			}.awaitAll()

			seasons.map {
				async(Dispatchers.IO) {
					burningSeriesDao.insertSeasonData(it.apply { this.seriesId = seriesId })
				}
			}.awaitAll()

			languages.map {
				async(Dispatchers.IO) {
					val langId = burningSeriesDao.addLanguageData(it, this@coroutineScope)
					burningSeriesDao.insertSeriesLanguagesCrossRef(SeriesLanguagesCrossRef(seriesId, langId))
				}
			}.awaitAll()

			episodes.map { entry ->
				async(Dispatchers.IO) {
					entry.key.seriesId = seriesId
					val episodeWatchProgress = watchProgress.getOrElse(entry.key.href) { 0L to 0L }
					if (entry.key.currentWatchPos < episodeWatchProgress.first) {
						entry.key.currentWatchPos = episodeWatchProgress.first
					}
					if (entry.key.totalWatchPos < episodeWatchProgress.second) {
						entry.key.totalWatchPos = episodeWatchProgress.second
					}
					val episodeId = burningSeriesDao.insertEpisodeInfo(entry.key)
					entry.value.map {
						async(Dispatchers.IO) {
							it.episodeId = episodeId
							burningSeriesDao.insertHoster(it)
						}
					}.awaitAll()
				}
			}.awaitAll()

			linkedSeries.map { linked ->
				async(Dispatchers.IO) {
					linked.seriesId = seriesId
					burningSeriesDao.insertSeriesLinkedSeries(linked)
				}
			}.awaitAll()
		}
	}

	suspend fun updateSeriesFavorite(seriesData: SeriesData) = burningSeriesDao.updateSeriesFavorite(seriesData.seriesId, seriesData.favoriteSince)

	suspend fun updateEpisodeInfo(episodeInfo: EpisodeInfo) {
		val newDBEpisode = burningSeriesDao.getEpisodeInfoByIdOrHref(episodeInfo.episodeId, episodeInfo.href).firstOrNull() ?: episodeInfo
		newDBEpisode.apply {
			currentWatchPos = episodeInfo.currentWatchPos
			totalWatchPos = episodeInfo.totalWatchPos
		}
		burningSeriesDao.updateEpisodeInfo(episodeInfo)
	}

	fun getSeriesFavorites(): Flow<List<SeriesWithInfo>> = burningSeriesDao.getSeriesFavorites().flowOn(Dispatchers.IO)

	fun searchSeriesFavorites(title: String): Flow<List<SeriesWithInfo>> = burningSeriesDao.searchFavorites(title).flowOn(Dispatchers.IO)

	fun getAllSeries(pagination: Long): Flow<Resource<List<GenreWithItems>>> = flow {
		if (pagination == 0L) {
			val first = burningSeriesDao.getAllSeries(pagination).first()
			val currentRequest = Clock.System.now().epochSeconds

			if (first.isEmpty() || first.any { currentRequest - Constants.DAY_IN_SECONDS >= it.genre.updatedAt }) {
				emit(Resource.loading(first))
				emitAll(networkBoundResource(
					fetchFromLocal = {
						burningSeriesDao.getAllSeries(pagination)
					},
					shouldFetchFromRemote = {
						it.isNullOrEmpty() || it.any { item -> currentRequest - Constants.DAY_IN_SECONDS >= item.genre.updatedAt }
					},
					fetchFromRemote = {
						service.getAllSeries()
					},
					saveRemoteData = { all ->
						saveGenreData(all)
					}
				))
			} else {
				emit(Resource.success(first))
			}
		} else {
			emit(Resource.success(burningSeriesDao.getAllSeries(pagination).first()))
		}
	}.flowOn(Dispatchers.IO)

	fun getAllSeriesCount() = burningSeriesDao.getAllSeriesCount().flowOn(Dispatchers.IO)

	fun searchAllSeries(title: String): Flow<List<GenreModel>> = flow<List<GenreModel>> {
		emitAll(burningSeriesDao.searchAllSeries(escapeSearchQuery(title), title).map {
			it.sortedWith(compareByDescending<GenreItemWithMatchInfo> { item ->
				item.genreItem.title.equals(title, true).toInt()
			}.thenByDescending { item ->
				item.genreItem.title.startsWith(title, true).toInt()
			}.thenByDescending { item ->
				item.genreItem.title.contains(title, true).toInt()
			}.thenByDescending { item -> item.matchInfo?.calculateScore() }).map { item ->
				item.genreItem
			}
		})
	}.flowOn(Dispatchers.IO)

	private fun escapeSearchQuery(query: String): String {
		val queryWithEscapedQuotes = query.replace(Regex.fromLiteral("\""), "\"\"")
		return "\"$queryWithEscapedQuotes\""
	}

	private suspend fun saveGenreData(genreList: List<GenreData>) {
		genreList.sortedBy { it.genre }.forEach { genreData ->
			val genreId: Long = burningSeriesDao.insertGenre(genreData)
			genreData.items.forEach { item ->
				item.genreId = genreId
				burningSeriesDao.insertGenreItem(item)
			}
		}
	}

	fun getAllSeriesCountJoined() = burningSeriesDao.getAllSeriesCountJoined()

	fun getAllGenres() = burningSeriesDao.getAllGenres()

	fun getSeriesCount() = flow<Long?> {
		networkResource(
			fetchFromRemote = {
				service.getSeriesCount()
			}
		).collect {
			emit(it.data?.toLongOrNull())
		}
	}.flowOn(Dispatchers.IO).distinctUntilChanged()

	fun saveScrapedHoster(scraped: ScrapeHoster): Flow<Boolean> = flow {
		val entry = jsonBuilder.encodeToString(
			scraped
		).toRequestBody(Constants.MEDIATYPE_JSON.toMediaType())
		networkResource(fetchFromRemote = {
			service.saveScraped(entry)
		}).collect {
			when (it.status) {
				Resource.Status.SUCCESS -> {
					if ((it.data?.failed ?: 0) <= 0) {
						emit(true)
					} else {
						emit(false)
					}
				}
				is Resource.Status.ERROR -> emit(false)
				else -> { }
			}
		}
	}.flowOn(Dispatchers.IO)

	fun getStreams(hrefList: List<String>) = flow<Resource<List<Stream>>> {
		val entries = jsonBuilder.encodeToString(
			hrefList
		).toRequestBody(Constants.MEDIATYPE_JSON.toMediaType())
		emitAll(networkResource(fetchFromRemote = {
			service.getStreams(entries)
		}))
	}.flowOn(Dispatchers.IO)
}