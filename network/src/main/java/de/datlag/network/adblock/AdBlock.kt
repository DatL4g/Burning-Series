package de.datlag.network.adblock

import com.hadiyarajesh.flower.ApiResponse
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

@Obfuscate
interface AdBlock {

    @GET
    fun getAdBlockList(@Url url: String): Flow<ApiResponse<ResponseBody>>
}