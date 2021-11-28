package de.datlag.burningseries.module

import com.hadiyarajesh.flower.calladpater.FlowCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.datlag.model.Constants
import de.datlag.network.burningseries.BurningSeries
import de.datlag.network.burningseries.BurningSeriesScraper
import de.datlag.network.jsonbase.JsonBase
import de.datlag.network.m3o.Image
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Obfuscate
object NetworkModule {
	
	private val jsonBuilder = Json {
		ignoreUnknownKeys = true
		isLenient = true
		encodeDefaults = true
	}
	
	private val loggingInterceptor = HttpLoggingInterceptor {
		Timber.w(it)
	}.apply {
		level = HttpLoggingInterceptor.Level.BASIC
	}
	
	@Provides
	@Named(Constants.NAMED_JSON)
	fun provideMediaType(): MediaType = MediaType.get(Constants.MEDIATYPE_JSON)
	
	@Provides
	@Singleton
	@Named(Constants.NAMED_JSON_CONVERTER)
	fun provideConverterFactory(
		@Named(Constants.NAMED_JSON) json: MediaType
	): Converter.Factory = jsonBuilder.asConverterFactory(json)
	
	@Provides
	@Singleton
	fun provideCallFactory(): OkHttpClient = OkHttpClient.Builder()
		.connectTimeout(60, TimeUnit.SECONDS)
		.readTimeout(60, TimeUnit.SECONDS)
		.writeTimeout(60, TimeUnit.SECONDS)
		.addInterceptor(loggingInterceptor)
		.build()
	
	@Provides
	@Singleton
	@Named(Constants.NAMED_JSON_RETROFIT)
	fun provideRetrofit(
		@Named(Constants.NAMED_JSON_CONVERTER) json: Converter.Factory,
		httpClient: OkHttpClient
	): Retrofit.Builder = Retrofit.Builder()
		.addCallAdapterFactory(FlowCallAdapterFactory())
		.callFactory(httpClient)
		.addConverterFactory(json)
	
	@Provides
	@Singleton
	fun provideBurningSeriesService(
		@Named(Constants.NAMED_JSON_RETROFIT) builder: Retrofit.Builder
	): BurningSeries = builder
		.baseUrl(Constants.API_WRAP_API_BURNING_SERIES)
		.build()
		.create(BurningSeries::class.java)

	@Provides
	@Singleton
	fun provideJsonBaseService(
		@Named(Constants.NAMED_JSON_RETROFIT) builder: Retrofit.Builder
	): JsonBase = builder
		.baseUrl(Constants.API_JSONBASE)
		.build()
		.create(JsonBase::class.java)

	@Provides
	@Singleton
	fun provideBurningSeriesScraper() = BurningSeriesScraper()
	
	@Provides
	@Singleton
	fun provideM3OImageService(
		@Named(Constants.NAMED_JSON_RETROFIT) builder: Retrofit.Builder
	): Image = builder
		.baseUrl(Constants.API_M3O)
		.build()
		.create(Image::class.java)
}
