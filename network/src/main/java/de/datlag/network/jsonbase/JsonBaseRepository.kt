package de.datlag.network.jsonbase

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.jsonbase.Stream
import de.datlag.network.common.toMD5
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@Obfuscate
class JsonBaseRepository @Inject constructor(
    private val service: JsonBase
) {

    fun getStream(href: String): Flow<Stream?> = flow {
        val id = href.toMD5()
        networkResource(
            fetchFromRemote = {
                service.getStream(id)
            }
        ).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> emit(it.data)
                Resource.Status.ERROR -> emit(null)
                else -> {}
            }
        }
    }
}