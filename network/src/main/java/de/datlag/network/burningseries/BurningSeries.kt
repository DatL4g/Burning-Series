package de.datlag.network.burningseries

import com.hadiyarajesh.flower.ApiResponse
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.AllSeries
import de.datlag.model.burningseries.home.Home
import de.datlag.model.burningseries.series.Series
import de.datlag.model.burningseries.series.SeriesData
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

@Obfuscate
interface BurningSeries {
	
	@Headers("Accept: ${Constants.MEDIATYPE_JSON}")
	@GET("${Constants.API_WRAP_API_PREFIX}/home/{version}")
	fun getHomeData(
		@Path("version") version: String = Constants.API_WRAP_API_HOME_VERSION,
		@Query("wrapAPIKey") apiKey: String
	): Flow<ApiResponse<Home>>

	@Headers("Accept: ${Constants.MEDIATYPE_JSON}")
	@GET("${Constants.API_WRAP_API_PREFIX}/series/{version}")
	fun getSeriesData(
		@Path("version") version: String = Constants.API_WRAP_API_SERIES_VERSION,
		@Query("wrapAPIKey") apiKey: String,
		@Query("serie") series: String
	): Flow<ApiResponse<Series>>

	@Headers("Accept: ${Constants.MEDIATYPE_JSON}")
	@GET("${Constants.API_WRAP_API_PREFIX}/all/{version}")
	fun getAllSeries(
		@Path("version") version: String = Constants.API_WRAP_API_ALL_VERSION,
		@Query("wrapAPIKey") apiKey: String
	): Flow<ApiResponse<AllSeries>>
}