package de.datlag.network.burningseries

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkBoundResource
import com.hadiyarajesh.flower.networkResource
import de.datlag.database.burningseries.BurningSeriesDao
import de.datlag.model.burningseries.home.HomeData
import de.datlag.model.burningseries.series.SeriesData
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named

@Obfuscate
class BurningSeriesRepository @Inject constructor(
	private val service: BurningSeries,
	@Named("wrapApiToken") val wrapApiToken: String,
	private val burningSeriesDao: BurningSeriesDao
) {

	fun getHomeData(): Flow<Resource<HomeData>> = flow {
		emit(Resource.loading(null))
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
	}.flowOn(Dispatchers.IO)

	fun getSeriesData(hrefTitle: String): Flow<Resource<SeriesData>> = networkBoundResource(
		fetchFromLocal = {
			burningSeriesDao.getSeriesByHrefTitle(hrefTitle)
		},
		shouldFetchFromRemote = {
			it == null
		},
		fetchFromRemote = {
			service.getSeriesData(
				apiKey = wrapApiToken,
				series = hrefTitle
			)
		},
		saveRemoteData = { series ->
			if (series.success) {
				series.data?.let { burningSeriesDao.insertSeriesData(it) }
			}
		}
	).flowOn(Dispatchers.IO)

	fun getSeriesFavorites(): Flow<List<SeriesData>> = burningSeriesDao.getSeriesFavorites().flowOn(Dispatchers.IO)
}