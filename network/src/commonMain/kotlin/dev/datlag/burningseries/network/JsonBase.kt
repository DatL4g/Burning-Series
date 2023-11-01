package dev.datlag.burningseries.network

import de.jensklingenberg.ktorfit.http.*
import dev.datlag.burningseries.model.JsonBaseCaptchaEntry

interface JsonBase {

    @Headers("Accept: application/json")
    @GET("bs-decaptcha/{id}")
    suspend fun burningSeriesCaptcha(
        @Path("id") id: String
    ): JsonBaseCaptchaEntry

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @PUT("bs-decaptcha/{id}")
    suspend fun setBurningSeriesCaptcha(
        @Path("id") id: String,
        @Body body: JsonBaseCaptchaEntry
    ): JsonBaseCaptchaEntry
}