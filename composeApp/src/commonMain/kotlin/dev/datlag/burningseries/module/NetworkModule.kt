package dev.datlag.burningseries.module

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.burningseries.BuildKonfig
import dev.datlag.burningseries.Sekret
import dev.datlag.burningseries.common.nullableFirebaseInstance
import dev.datlag.burningseries.github.GitHub
import dev.datlag.burningseries.github.UserAndReleaseRepository
import dev.datlag.burningseries.network.EpisodeStateMachine
import dev.datlag.burningseries.network.HomeStateMachine
import dev.datlag.burningseries.network.SaveStateMachine
import dev.datlag.burningseries.network.SearchStateMachine
import dev.datlag.burningseries.network.SeriesStateMachine
import dev.datlag.burningseries.other.UserHelper
import dev.datlag.tooling.compose.ioDispatcher
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import okio.FileSystem
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow

data object NetworkModule {

    const val NAME = "NetworkModule"

    @OptIn(ExperimentalOpenIdConnect::class)
    val di = DI.Module(NAME) {
        import(DatabaseModule.di)

        bindSingleton<Json> {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
        bindSingleton<ImageLoader> {
            ImageLoader.Builder(instance<PlatformContext>())
                .components {
                    add(KtorNetworkFetcherFactory(instance<HttpClient>()))
                    add(SvgDecoder.Factory())
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(instance<PlatformContext>())
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
                        .maxSizeBytes(512L * 1024 * 1024) // 512 MB
                        .build()
                }
                .crossfade(true)
                .extendImageLoader()
                .build()
        }
        bindProvider<HomeStateMachine> {
            HomeStateMachine(
                client = instance()
            )
        }
        bindProvider<SearchStateMachine> {
            SearchStateMachine(
                client = instance()
            )
        }
        bindProvider<SeriesStateMachine> {
            SeriesStateMachine(
                client = instance()
            )
        }
        bindProvider<EpisodeStateMachine> {
            EpisodeStateMachine(
                client = instance(),
                firebaseAuth = nullableFirebaseInstance()?.auth,
                fireStore = nullableFirebaseInstance()?.store
            )
        }
        bindProvider<SaveStateMachine> {
            SaveStateMachine(
                client = instance(),
                firebaseAuth = nullableFirebaseInstance()?.auth,
                fireStore = nullableFirebaseInstance()?.store
            )
        }
        bindSingleton<ApolloClient> {
            val userHelper = instance<UserHelper>()

            ApolloClient.Builder()
                .dispatcher(ioDispatcher())
                .serverUrl("https://api.github.com/graphql")
                .addHttpInterceptor(object : HttpInterceptor {
                    override suspend fun intercept(
                        request: HttpRequest,
                        chain: HttpInterceptorChain
                    ): HttpResponse {
                        val req = request.newBuilder().apply {
                            val token = userHelper.getAccessToken()

                            token?.let {
                                addHeader("Authorization", "Bearer $it")
                            }
                        }.build()

                        return chain.proceed(req)
                    }
                })
                .build()
        }
        bindSingleton<GitHub> {
            val ktorfit = ktorfit {
                httpClient(instance<HttpClient>())
                baseUrl("https://api.github.com/")
            }
            ktorfit.create<GitHub>()
        }
        bindSingleton<UserAndReleaseRepository> {
            UserAndReleaseRepository(
                client = instance(),
                github = instance()
            )
        }
        bindSingleton<OpenIdConnectClient> {
            OpenIdConnectClient {
                endpoints {
                    authorizationEndpoint = "https://github.com/login/oauth/authorize"
                    tokenEndpoint = "https://github.com/login/oauth/access_token"
                }

                clientId = Sekret.githubClientId(BuildKonfig.packageName)
                clientSecret = Sekret.githubClientSecret(BuildKonfig.packageName)
                scope = "read:user"
            }
        }
        bindProvider<CodeAuthFlow> {
            instance<CodeAuthFlowFactory>().createAuthFlow(instance())
        }
        bindSingleton<UserHelper> {
            UserHelper(
                oidcClient = instance(),
                tokenStore = instance()
            )
        }
    }
}