package dev.datlag.burningseries.module

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfitBuilder
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.GitHub
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.converter.FlowerResponseConverter
import dev.datlag.burningseries.network.repository.*
import dev.datlag.burningseries.other.EasyDns
import dev.datlag.burningseries.other.MultiDoH
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
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
    private const val TAG_KTORFIT_JSONBASE = "JsonBaseKtorfit"
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
            } catch (ignored: Throwable) { }

            tempDir
        }

        bindSingleton(TAG_OKHTTP_BOOTSTRAP_CLIENT) {
            OkHttpClient.Builder().cache(Cache(instance(TAG_OKHTTP_CACHE_FOLDER), 4096)).build()
        }

        bindSingleton {
            val bootstrapClient: OkHttpClient = instance(TAG_OKHTTP_BOOTSTRAP_CLIENT)
            MultiDoH(
                DnsOverHttps.Builder().client(bootstrapClient)
                    .url("https://dns.google/dns-query".toHttpUrl())
                    .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
                    .build(),
                DnsOverHttps.Builder().client(bootstrapClient)
                    .url("https://dns.google/dns-query".toHttpUrl())
                    .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
                    .post(true)
                    .build(),
                DnsOverHttps.Builder().client(bootstrapClient)
                    .url("https://1.1.1.1/dns-query".toHttpUrl())
                    .bootstrapDnsHosts(InetAddress.getByName("1.1.1.1"), InetAddress.getByName("1.0.0.1"))
                    .includeIPv6(false)
                    .build(),
                DnsOverHttps.Builder().client(bootstrapClient)
                    .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
                    .bootstrapDnsHosts(InetAddress.getByName("1.1.1.1"), InetAddress.getByName("1.0.0.1"))
                    .includeIPv6(false)
                    .post(true)
                    .build(),
                DnsOverHttps.Builder().client(bootstrapClient)
                    .url("https://dns.dnsoverhttps.net/dns-query".toHttpUrl())
                    .includeIPv6(false)
                    .build(),
                EasyDns(),
                Dns.SYSTEM
            )
        }

        bindSingleton {
            instance<OkHttpClient>(TAG_OKHTTP_BOOTSTRAP_CLIENT).newBuilder()
                .dns(instance())
                .build()
        }

        bindSingleton {
            ktorfitBuilder {
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
                            isLenient = true
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
        bindSingleton(TAG_KTORFIT_JSONBASE) {
            val builder: Ktorfit.Builder = instance()
            builder.build {
                baseUrl("https://jsonbase.com/")
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
            val jsonBaseKtor: Ktorfit = instance(TAG_KTORFIT_JSONBASE)
            jsonBaseKtor.create<JsonBase>()
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
            EpisodeRepository(instance(), instance(), instance())
        }
        bindProvider {
            SaveRepository(instance(), instance(), instance())
        }
        bindSingleton {
            GitHubRepository(instance())
        }
    }
}