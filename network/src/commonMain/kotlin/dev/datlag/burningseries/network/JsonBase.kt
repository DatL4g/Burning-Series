package dev.datlag.burningseries.network

import com.hadiyarajesh.flower_core.ApiResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers

interface JsonBase {

    @Headers("Accept: application/json")
    @GET("bs-decaptcha/{id}")
    suspend fun burningSeriesCaptcha(
        id: String
    ): ApiResponse<Any>
}