package de.datlag.network.m3o

import android.util.Log
import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.common.base64ToByteArray
import de.datlag.model.m3o.image.Convert
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Named

@Obfuscate
class M3ORepository @Inject constructor(
	val service: Image,
	@Named("m3oToken") val token: String
) {

	private fun convertImageUrl(url: String): Flow<Resource<Convert>> {
		return networkResource(
			fetchFromRemote = {
				service.convertURL("Bearer $token", Convert.RequestURL(url))
			}
		).flowOn(Dispatchers.IO)
	}
	
	fun getImageFromURL(url: String, isConvertFetch: Boolean = false): Flow<Resource<ByteArray>> = flow {
		fun loadWithConvert(responseBody: Resource<ResponseBody>): Flow<Resource<ByteArray>> = flow {
			convertImageUrl(url).collect { convert ->
				when (convert.status) {
					Resource.Status.ERROR -> {
						emit(Resource.error(
							convert.message ?: responseBody.message ?: "Error loading image $url",
							null
						))
					}
					Resource.Status.SUCCESS -> {
						if (!convert.data?.base64.isNullOrEmpty()) {
							emit(Resource.success(convert.data!!.base64.base64ToByteArray()))
						} else if (!convert.data?.url.isNullOrEmpty() && !isConvertFetch) {
							emitAll(getImageFromURL(convert.data!!.url, true))
						}
					}
					else -> emit(Resource.loading(null))
				}
			}
		}
		
		emit(Resource.loading(null))
		networkResource(
			fetchFromRemote = {
				service.getImageFromURL(url)
			}
		).collect { responseBody ->
			when (responseBody.status) {
				Resource.Status.ERROR -> {
					emitAll(loadWithConvert(responseBody))
				}
				Resource.Status.SUCCESS -> {
					if (responseBody.data != null) {
						responseBody.data!!.byteStream().use { stream ->
							emit(Resource.success(stream.readBytes()))
						}
					} else {
						emitAll(loadWithConvert(responseBody))
					}
				}
				else -> Resource.loading(null)
			}
		}
	}.flowOn(Dispatchers.IO)
}