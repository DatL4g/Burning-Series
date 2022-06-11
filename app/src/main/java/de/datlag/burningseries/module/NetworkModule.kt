package de.datlag.burningseries.module

import com.apollographql.apollo3.ApolloClient
import com.hadiyarajesh.flower.calladpater.FlowCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.datlag.model.Constants
import de.datlag.network.adblock.AdBlock
import de.datlag.network.burningseries.BurningSeries
import de.datlag.network.github.GitHub
import de.datlag.network.video.VideoScraper
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
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

	val jsonBuilder = Json {
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
	@Singleton
	fun provideJsonBuilder(): Json = jsonBuilder

	@Provides
	@Named(Constants.NAMED_JSON)
	fun provideMediaType(): MediaType = Constants.MEDIATYPE_JSON.toMediaType()

	@Provides
	@Singleton
	@Named(Constants.NAMED_JSON_CONVERTER)
	fun provideConverterFactory(
		@Named(Constants.NAMED_JSON) json: MediaType
	): Converter.Factory = jsonBuilder.asConverterFactory(json)

	@Provides
	@Singleton
	fun provideCallFactory(): OkHttpClient = OkHttpClient.Builder()
		.connectTimeout(2, TimeUnit.MINUTES)
		.readTimeout(2, TimeUnit.MINUTES)
		.writeTimeout(2, TimeUnit.MINUTES)
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
		.baseUrl("https://api.datlag.dev")
		.build()
		.create(BurningSeries::class.java)

	@Provides
	@Singleton
	fun provideVideoScraper() = VideoScraper()

	@Provides
	@Singleton
	fun provideAdBlockService(
		@Named(Constants.NAMED_JSON_RETROFIT) builder: Retrofit.Builder
	): AdBlock = builder
		.build()
		.create(AdBlock::class.java)

	@Provides
	@Singleton
	fun provideGitHubService(
		@Named(Constants.NAMED_JSON_RETROFIT) builder: Retrofit.Builder
	): GitHub = builder
		.baseUrl(Constants.API_GITHUB)
		.build()
		.create(GitHub::class.java)

	@Provides
	@Singleton
	@Named("anilistApollo")
	fun provideAniListApolloClient() = ApolloClient.Builder().serverUrl("https://graphql.anilist.co").build()

	@Provides
	@Singleton
	@Named("githubApollo")
	fun provideGitHubApolloClient() = ApolloClient.Builder().serverUrl("https://api.github.com/graphql").build()
}
