package dev.datlag.burningseries.network

import com.hadiyarajesh.flower_core.ApiResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.Path
import dev.datlag.burningseries.model.JsonBaseCaptchaEntry

interface JsonBase {

    @Headers("Accept: application/json")
    @GET("bs-decaptcha/{id}")
    suspend fun burningSeriesCaptcha(
        @Path("id") id: String
    ): ApiResponse<JsonBaseCaptchaEntry>
}