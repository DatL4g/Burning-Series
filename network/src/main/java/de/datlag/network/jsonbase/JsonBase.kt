package de.datlag.network.jsonbase

import com.hadiyarajesh.flower.ApiResponse
import de.datlag.model.Constants
import de.datlag.model.jsonbase.Stream
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

@Obfuscate
interface JsonBase {

    @Headers("Accept: ${Constants.MEDIATYPE_JSON}")
    @GET("${Constants.API_JSONBASE_PREFIX}/{id}")
    fun getStream(@Path("id") id: String): Flow<ApiResponse<Stream>>
}