package de.datlag.network.burningseries

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.burningseries.home.HomeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class BurningSeriesRepository @Inject constructor(
	private val service: BurningSeries
) {

	fun getHomeData(): Flow<Resource<HomeData>> = flow {
		emit(Resource.loading(null))
		networkResource(
			fetchFromRemote = {
				service.getHomeData()
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
}