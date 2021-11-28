package de.datlag.network.jsonbase

import com.hadiyarajesh.flower.ApiResponse
import de.datlag.model.Constants
import de.datlag.model.jsonbase.BsHoster
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

@Obfuscate
interface JsonBase {

    @Headers("Accept: ${Constants.MEDIATYPE_JSON}")
    @GET("/bs-decaptcha/{id}")
    fun getBsHosterData(@Path("id") id: String): Flow<ApiResponse<BsHoster>>
}