package dev.datlag.burningseries.module

import androidx.datastore.core.DataStore
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfitBuilder
import dev.datlag.burningseries.common.cookieMap
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.datastore.preferences.UserSettings
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.GitHub
import dev.datlag.burningseries.network.converter.FlowerResponseConverter
import dev.datlag.burningseries.network.repository.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.dnsoverhttps.DnsOverHttps
import org.kodein.di.*
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory

object NetworkModule {

    private const val TAG_KTORFIT_BURNINGSERIES = "BurningSeriesKtorfit"
    private const val TAG_KTORFIT_GITHUB = "GitHubKtorfit"
    const val TAG_OKHTTP_CACHE_FOLDER = "OkHttpCacheFolder"
    const val TAG_OKHTTP_BOOTSTRAP_CLIENT = "OkHttpBootstrapClient"
    const val NAME = "NetworkModule"

    val di = DI.Module(NAME) {
        import(DataStoreModule.di)

        bindSingleton(TAG_OKHTTP_CACHE_FOLDER) {
            val tempDir = createTempDirectory(
                prefix = "httpDnsCache"
            ).toFile()

            try {
                tempDir.deleteOnExit()
            } catch (ignored: Throwable) {
            }

            tempDir
        }

        bindSingleton(TAG_OKHTTP_BOOTSTRAP_CLIENT) {
            okhttp3.OkHttpClient.Builder().cache(Cache(instance(TAG_OKHTTP_CACHE_FOLDER), 4096)).build()
        }

        bindSingleton {
            DnsOverHttps.Builder().client(instance(TAG_OKHTTP_BOOTSTRAP_CLIENT))
                .url("https://dns.google/dns-query".toHttpUrl())
                .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
                .build()
        }

        bindSingleton {
            instance<OkHttpClient>(TAG_OKHTTP_BOOTSTRAP_CLIENT).newBuilder()
                .dns(instance())
                .build()
        }

        bindSingleton {
            val userSettings: DataStore<UserSettings> = instance()
            val userCookies = userSettings.cookieMap.getValueBlocking(emptyList())

            object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {}

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    val list = userCookies.toMap().entries.map { (t, u) ->
                        Cookie.Builder()
                            .name(t)
                            .value(u)
                            .domain("api.datlag.dev")
                            .build()
                    }

                    return list
                }
            }
        }

        bindSingleton {
            ktorfitBuilder {
                responseConverter(FlowerResponseConverter())
                httpClient(OkHttp) {
                    engine {
                        config {
                            cookieJar(instance())
                            dns(instance())
                            connectTimeout(3, TimeUnit.MINUTES)
                            readTimeout(3, TimeUnit.MINUTES)
                            writeTimeout(3, TimeUnit.MINUTES)
                        }
                    }
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            isLenient = false
                        })
                    }
                }
            }
        }
        bindSingleton(TAG_KTORFIT_BURNINGSERIES) {
            val builder: Ktorfit.Builder = instance()
            builder.build {
                baseUrl("https://api.datlag.dev/bs/")
            }
        }
        bindSingleton(TAG_KTORFIT_GITHUB) {
            val builder: Ktorfit.Builder = instance()
            builder.build {
                baseUrl("https://api.github.com/")
            }
        }
        bindSingleton {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
        bindSingleton {
            val bsKtor: Ktorfit = instance(TAG_KTORFIT_BURNINGSERIES)
            bsKtor.create<BurningSeries>()
        }
        bindSingleton {
            val githubKtor: Ktorfit = instance(TAG_KTORFIT_GITHUB)
            githubKtor.create<GitHub>()
        }
        bindSingleton {
            HomeRepository(instance())
        }
        bindSingleton {
            GenreRepository(instance())
        }
        bindProvider {
            SeriesRepository(instance())
        }
        bindProvider {
            EpisodeRepository(instance(), instance())
        }
        bindProvider {
            SaveRepository(instance(), instance())
        }
        bindSingleton {
            GitHubRepository(instance())
        }
    }
}