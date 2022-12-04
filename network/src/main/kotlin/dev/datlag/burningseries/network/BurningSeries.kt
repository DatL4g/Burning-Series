package dev.datlag.burningseries.network

import com.hadiyarajesh.flower_core.ApiResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Path
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.model.LoginCookie
import kotlinx.serialization.json.JsonElement

interface BurningSeries {

    @GET("home")
    suspend fun home(): ApiResponse<Home>

    @GET("all")
    suspend fun all(): ApiResponse<List<Genre>>

    @GET("login")
    suspend fun login(
        @Header("user") user: String
    ): ApiResponse<LoginCookie>

    @GET("series/{href}")
    suspend fun series(
        @Path("href") href: String
    ): ApiResponse<Series>
}