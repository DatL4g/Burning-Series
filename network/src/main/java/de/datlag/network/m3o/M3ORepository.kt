package de.datlag.network.m3o

import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.model.Constants
import de.datlag.model.burningseries.series.HosterData
import de.datlag.model.common.base64ToByteArray
import de.datlag.model.jsonbase.Stream
import de.datlag.model.m3o.db.CountRequest
import de.datlag.model.m3o.db.create.BurningSeriesHoster
import de.datlag.model.m3o.db.create.BurningSeriesHosterRecord
import de.datlag.model.m3o.db.read.BurningSeriesHosterQuery
import de.datlag.model.m3o.image.Convert
import de.datlag.model.m3o.image.ConvertRequestURL
import de.datlag.model.video.ScrapeHoster
import de.datlag.network.jsonbase.JsonBaseRepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Named

@Obfuscate
class M3ORepository @Inject constructor(
	val imageService: Image,
	val dbService: DB,
	val jsonBaseRepository: JsonBaseRepository,
	@Named("m3oToken") val token: String,
	val jsonBuilder: Json
) {

	private fun convertImageUrl(url: String): Flow<Resource<Convert>> {
		return networkResource(
			fetchFromRemote = {
				imageService.convertURL(
					"Bearer $token",
					jsonBuilder.encodeToString(
						ConvertRequestURL(url)
					).toRequestBody(Constants.MEDIATYPE_JSON.toMediaType())
				)
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
				imageService.getImageFromURL(url)
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

	fun getBurningSeriesHosterCount(): Flow<Long?> = flow {
		emit(null)
		networkResource(
			fetchFromRemote = {
				dbService.countBurningSeries(
					"Bearer $token",
					jsonBuilder.encodeToString(
						CountRequest(Constants.M3O_BURNING_SERIES_TABLE)
					).toRequestBody(Constants.MEDIATYPE_JSON.toMediaType())
				)
			}
		).collect {
			it.data?.count?.let { count -> emit(count) }
		}
	}.flowOn(Dispatchers.IO)

	fun getAnyStream(list: List<HosterData>): Flow<Resource<List<Stream>>> = flow<Resource<List<Stream>>> {
		emit(Resource.loading(null))
		coroutineScope {
			val completeList = list.map {
				async { getStream(it.title, it.href).first() }
			}.awaitAll().filterNotNull()

			if (completeList.isEmpty()) {
				emit(Resource.error("No Hoster"))
			} else {
				emit(Resource.success(completeList))
			}
		}
	}.flowOn(Dispatchers.IO)

	private fun getStream(hoster: String, href: String): Flow<Stream?> = flow {
		networkResource(
			fetchFromRemote = {
				dbService.getStream(
					"Bearer $token",
					jsonBuilder.encodeToString(
						BurningSeriesHosterQuery(href)
					).toRequestBody(Constants.MEDIATYPE_JSON.toMediaType())
				)
			}
		).collect {
			when (it.status) {
				Resource.Status.SUCCESS -> {
					if (it.data?.records?.isNullOrEmpty() == true) {
						emitAll(getJsonBaseStreamAndSave(hoster, href))
					} else {
						emit(it.data!!.records[0].toStream(hoster))
					}
				}
				Resource.Status.ERROR -> {
					emitAll(getJsonBaseStreamAndSave(hoster, href))
				}
				Resource.Status.LOADING -> {}
			}
		}
	}.flowOn(Dispatchers.IO)

	private fun getJsonBaseStreamAndSave(hoster: String, href: String): Flow<Stream?> = flow {
		jsonBaseRepository.getStream(href).collect { stream ->
			if (stream != null) {
				saveStream(href, stream)
				emit(stream.apply { this.hoster = hoster })
			} else {
				emit(null)
			}
		}
	}.flowOn(Dispatchers.IO)

	fun saveScrapedHoster(scraped: ScrapeHoster): Flow<Boolean> = flow {
		val entry = jsonBuilder.encodeToString(
			BurningSeriesHoster(record = BurningSeriesHosterRecord.fromScraped(scraped))
		).toRequestBody(Constants.MEDIATYPE_JSON.toMediaType())
		networkResource(fetchFromRemote = {
			dbService.saveStream(
				"Bearer $token",
				entry
			)
		}).collect {
			when (it.status) {
				Resource.Status.SUCCESS -> emit(true)
				Resource.Status.ERROR -> {
					emitAll(updateScrapedHoster(entry))
				}
				else -> { }
			}
		}
	}.flowOn(Dispatchers.IO)

	private suspend fun updateScrapedHoster(entry: RequestBody): Flow<Boolean> = flow {
		networkResource(fetchFromRemote = {
			dbService.updateStream(
				"Bearer $token",
				entry
			)
		}).collect {
			when (it.status) {
				Resource.Status.SUCCESS -> emit(true)
				Resource.Status.ERROR -> {
					emit(false)
					if (!it.message.isNullOrEmpty()) {
						throw Exception(it.message)
					}
				}
				else -> { }
			}
		}
	}

	private suspend fun saveStream(href: String, stream: Stream) {
		networkResource(fetchFromRemote = {
			dbService.saveStream(
				"Bearer $token",
				jsonBuilder.encodeToString(
					BurningSeriesHoster(record = BurningSeriesHosterRecord.fromStream(href, stream))
				).toRequestBody(Constants.MEDIATYPE_JSON.toMediaType())
			)
		}).collect {
			if (it.status == Resource.Status.ERROR) {
				if (!it.message.isNullOrEmpty()) {
					throw Exception(it.message)
				}
			}
		}
	}
}