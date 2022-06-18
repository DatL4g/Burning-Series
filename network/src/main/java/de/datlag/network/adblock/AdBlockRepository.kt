package de.datlag.network.adblock

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.InputStream
import javax.inject.Inject

@Obfuscate
class AdBlockRepository @Inject constructor(
    private val service: AdBlock
) {

    fun getAdBlockList(): Flow<Resource<InputStream>> = flow<Resource<InputStream>> {
        networkResource(fetchFromRemote = {
            service.getAdBlockList(Constants.URL_ADBLOCK_LIST)
        }).collect {
            when (it.status) {
                Resource.Status.LOADING -> emit(Resource.loading(null))
                is Resource.Status.ERROR -> {
                    val errorStatus = it.status as Resource.Status.ERROR
                    emit(Resource.error(errorStatus.message, errorStatus.statusCode))
                }
                Resource.Status.SUCCESS -> emit(Resource.success(it.data?.byteStream()))
            }
        }
    }.flowOn(Dispatchers.IO)
}