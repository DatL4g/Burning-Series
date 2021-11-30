package de.datlag.network.m3o

import com.hadiyarajesh.flower.ApiResponse
import de.datlag.model.Constants
import de.datlag.model.m3o.db.Count
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

@Obfuscate
interface DB {

    @Headers("Content-Type: ${Constants.MEDIATYPE_JSON}")
    @POST("/v1/db/Count")
    fun countBurningSeries(@Header("Authorization") token: String, @Body body: Count.Request): Flow<ApiResponse<Count>>
}