package de.datlag.network.burningseries

import android.util.Log
import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkBoundResource
import com.hadiyarajesh.flower.networkResource
import de.datlag.database.burningseries.BurningSeriesDao
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.allseries.relation.GenreWithItems
import de.datlag.model.burningseries.home.HomeData
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.series.SeasonData
import de.datlag.model.burningseries.series.SeriesData
import de.datlag.model.burningseries.series.relation.SeriesLanguagesCrossRef
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.network.common.mapInPlace
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
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
		emit(Resource.loading(null))
		val scrapeData = scraper.scrapeHomeData()

		if (scrapeData != null) {
			emit(Resource.success(scrapeData))
		} else {
			networkResource(
				fetchFromRemote = {
					service.getHomeData(apiKey = wrapApiToken)
				}
			).collect {
				if (it.status == Resource.Status.LOADING) {
					emit(Resource.loading(it.data?.data))
				} else {
					if (it.data == null || !it.data!!.success) {
						emit(Resource.error(it.message ?: "Data error", it.data?.data))
					} else {
						emit(Resource.success(it.data?.data))
					}
				}
			}
		}
	}.flowOn(Dispatchers.IO)

	fun getSeriesData(latestSeries: LatestSeries) = getSeriesData(latestSeries.href, latestSeries.getHrefTitle())

	fun getSeriesData(latestEpisode: LatestEpisode) = getSeriesData(latestEpisode.getHrefWithoutEpisode(), latestEpisode.getHrefTitle())

	fun getSeriesData(genreItem: GenreModel.GenreItem) = getSeriesData(genreItem.href, genreItem.getHrefTitle())

	private fun getSeriesData(href: String, hrefTitle: String): Flow<Resource<SeriesWithInfo?>> = flow {
		emit(Resource.loading(burningSeriesDao.getSeriesWithInfoByHrefTitle(hrefTitle).first()))
		val scrapeData = scraper.scrapeSeriesData(href)

		if (scrapeData != null) {
			saveSeriesData(scrapeData)
			emitAll(burningSeriesDao.getSeriesWithInfoByHrefTitle(hrefTitle).map { Resource.success(it) })
		} else {
			Log.e("scrape data", "is null")
			val currentRequest = Clock.System.now().epochSeconds
			emitAll(networkBoundResource(
				fetchFromLocal = {
					burningSeriesDao.getSeriesWithInfoByHrefTitle(hrefTitle)
				},
				shouldFetchFromRemote = {
					it == null || (currentRequest - Constants.DAY_IN_MILLI) >= it.series.updatedAt || it.episodes.isEmpty()
				},
				fetchFromRemote = {
					service.getSeriesData(
						apiKey = wrapApiToken,
						series = hrefTitle
					)
				},
				saveRemoteData = { series ->
					if (series.success) {
						series.data?.let { saveSeriesData(it) }
					}
				}
			))
		}
	}.flowOn(Dispatchers.IO)

	suspend fun saveSeriesData(seriesData: SeriesData) {
		Log.e("seriesData", seriesData.hrefTitle)
		val prevFavoriteSince = burningSeriesDao.getSeriesFavoriteSinceByHrefTitle(seriesData.hrefTitle).first() ?: 0L
		seriesData.favoriteSince = prevFavoriteSince
		val seriesId = burningSeriesDao.insertSeriesData(seriesData)
		seriesData.infos.forEach {
			it.seriesId = seriesId
			burningSeriesDao.insertInfoData(it)
		}
		seriesData.seasons.forEach {
			burningSeriesDao.insertSeasonData(SeasonData(it, seriesId))
		}
		seriesData.languages.forEach {
			val langId = burningSeriesDao.addLanguageData(it)
			burningSeriesDao.insertSeriesLanguagesCrossRef(SeriesLanguagesCrossRef(seriesId, langId))
		}
		seriesData.episodes.forEach { episode ->
			episode.seriesId = seriesId
			val episodeId = burningSeriesDao.insertEpisodeInfo(episode)
			episode.hoster.forEach {
				it.episodeId = episodeId
				burningSeriesDao.insertHoster(it)
			}
		}
	}

	suspend fun updateSeriesFavorite(seriesData: SeriesData) = burningSeriesDao.updateSeriesFavorite(seriesData.seriesId, seriesData.favoriteSince)

	fun getSeriesFavorites(): Flow<List<SeriesWithInfo>> = burningSeriesDao.getSeriesFavorites().flowOn(Dispatchers.IO)

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
							it == null || it.any { item -> (currentRequest - Constants.DAY_IN_MILLI) >= item.genre.updatedAt }
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

	fun getAllSeriesCount() = burningSeriesDao.getAllSeriesCount()

	suspend fun saveGenreData(genreList: List<GenreModel.GenreData>) {
		genreList.sortedBy { it.genre }.forEach { genreData ->
			val genreId: Long = burningSeriesDao.insertGenre(genreData)
			genreData.items.forEach { item ->
				item.genreId = genreId
				burningSeriesDao.insertGenreItem(item)
			}
		}
	}
}