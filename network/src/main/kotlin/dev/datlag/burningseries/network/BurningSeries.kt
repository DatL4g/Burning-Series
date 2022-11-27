package dev.datlag.burningseries.network

import com.hadiyarajesh.flower_core.ApiResponse
import de.jensklingenberg.ktorfit.http.GET
import dev.datlag.burningseries.network.model.Home

interface BurningSeries {

    @GET("home")
    suspend fun home(): ApiResponse<Home>
}