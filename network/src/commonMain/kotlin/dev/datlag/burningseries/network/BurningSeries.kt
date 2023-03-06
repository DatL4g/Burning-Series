package dev.datlag.burningseries.network

import com.hadiyarajesh.flower_core.ApiResponse
import de.jensklingenberg.ktorfit.http.*
import dev.datlag.burningseries.model.*

interface BurningSeries {

    @Headers("Accept: application/json")
    @GET("home")
    suspend fun home(): ApiResponse<Home>

    @Headers("Accept: application/json")
    @GET("all")
    suspend fun all(): ApiResponse<List<Genre>>

    @Headers("Accept: application/json")
    @GET("series/{href}")
    suspend fun series(
        @Path("href") href: String,
    ): ApiResponse<Series>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("video/streams")
    suspend fun hosterStreams(
        @Body body: List<String>
    ): ApiResponse<List<HosterStream>>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("video")
    suspend fun save(
        @Body body: ScrapedHoster
    ): ApiResponse<InsertStream>
}