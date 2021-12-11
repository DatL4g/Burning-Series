package de.datlag.network.video

import com.hadiyarajesh.flower.ApiResponse
import de.datlag.model.Constants
import de.datlag.model.video.DownloadVideo
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

@Obfuscate
interface DownloadVideo {

    @Headers("Accept: ${Constants.MEDIATYPE_JSON}")
    @GET("${Constants.API_WRAP_API_VIDEO_PREFIX}/download-video/{version}")
    fun getDownloadVideo(
        @Path("version") version: String = Constants.API_WRAP_API_DOWNLOAD_VIDEO,
        @Query("wrapAPIKey") apiKey: String,
        @Query("site") site: String
    ) : Flow<ApiResponse<DownloadVideo>>
}