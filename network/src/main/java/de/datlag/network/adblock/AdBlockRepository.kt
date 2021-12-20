package de.datlag.network.adblock

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.InputStream
import javax.inject.Inject

class AdBlockRepository @Inject constructor(
    private val service: AdBlock
) {

    fun getAdBlockList(): Flow<Resource<InputStream>> = flow<Resource<InputStream>> {
        networkResource(fetchFromRemote = {
            service.getAdBlockList(Constants.URL_ADBLOCK_LIST)
        }).collect {
            when (it.status) {
                Resource.Status.LOADING -> emit(Resource.loading(null))
                Resource.Status.ERROR -> emit(Resource.error(it.message ?: it.status.toString()))
                Resource.Status.SUCCESS -> emit(Resource.success(it.data?.byteStream()))
            }
        }
    }.flowOn(Dispatchers.IO)
}