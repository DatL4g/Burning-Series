package dev.datlag.burningseries.module

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.burningseries.network.converter.FlowerResponseConverter
import org.kodein.di.*
import dev.datlag.burningseries.network.createBurningSeries
import dev.datlag.burningseries.network.repository.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.GlobalScope
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val TAG_KTORFIT_BURNINGSERIES = "BurningSeriesKtorfit"
    const val NAME = "NetworkModule"

    val di = DI.Module(NAME) {
        bindSingleton {
            // ToDo("add cache file")
            val bootstrapClient = okhttp3.OkHttpClient.Builder().build()
            DnsOverHttps.Builder().client(bootstrapClient)
                .url("https://dns.google/dns-query".toHttpUrl())
                .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
                .build()
        }

        bindSingleton(TAG_KTORFIT_BURNINGSERIES) {
            ktorfit {
                baseUrl("https://api.datlag.dev/bs/")
                responseConverter(FlowerResponseConverter())
                httpClient(OkHttp) {
                    engine {
                        config {
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
        bindSingleton {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
        bindSingleton {
            val bsKtor: Ktorfit = instance(TAG_KTORFIT_BURNINGSERIES)
            bsKtor.createBurningSeries()
        }
        bindSingleton {
            HomeRepository(instance())
        }
        bindSingleton {
            GenreRepository(instance())
        }
        bindSingleton {
            UserRepository(instance(), instance())
        }
        bindProvider {
            SeriesRepository(instance())
        }
        bindProvider {
            EpisodeRepository(instance())
        }
    }
}