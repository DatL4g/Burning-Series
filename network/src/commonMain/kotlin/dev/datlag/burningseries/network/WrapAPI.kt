package dev.datlag.burningseries.network

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import dev.datlag.burningseries.model.WrapAPIResponse

interface WrapAPI {

    @GET("DatLag/burning-series/home/latest")
    suspend fun getBurningSeriesHome(@Query("wrapAPIKey") apiKey: String): WrapAPIResponse

    @GET("DatLag/burning-series/series/latest")
    suspend fun getBurningSeries(@Query("wrapAPIKey") apiKey: String, @Query("href") href: String): WrapAPIResponse

    @GET("DatLag/burning-series/search/latest")
    suspend fun getBurningSeriesSearch(@Query("wrapAPIKey") apiKey: String): WrapAPIResponse
}