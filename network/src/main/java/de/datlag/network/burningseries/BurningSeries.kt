package de.datlag.network.burningseries

import com.hadiyarajesh.flower.ApiResponse
import de.datlag.model.Constants
import de.datlag.model.burningseries.home.Home
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface BurningSeries {
	
	@Headers("Accept: ${Constants.MEDIATYPE_JSON}")
	@GET("${Constants.API_WRAP_API_PREFIX}/home/{version}")
	fun getHomeData(
		@Path("version") version: String = Constants.API_WRAP_API_HOME_VERSION,
		@Query("wrapAPIKey") apiKey: String = Constants.API_WRAP_API_KEY
	): Flow<ApiResponse<Home>>
	
}