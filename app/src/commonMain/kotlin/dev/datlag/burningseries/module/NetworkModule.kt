package dev.datlag.burningseries.module

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.burningseries.network.converter.FlowerResponseConverter
import dev.datlag.burningseries.network.repository.HomeRepository
import org.kodein.di.*
import dev.datlag.burningseries.network.createBurningSeries
import dev.datlag.burningseries.network.repository.GenreRepository
import dev.datlag.burningseries.network.repository.SeriesRepository
import dev.datlag.burningseries.network.repository.UserRepository
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.GlobalScope
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress

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
                        }
                    }
                    install(ContentNegotiation) {
                        json()
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
        bindSingleton {
            SeriesRepository(instance())
        }
    }
}