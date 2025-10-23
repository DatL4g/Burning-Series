package dev.datlag.mimasu.extension.module

import dev.datlag.mimasu.extension.AppInitializer
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.tooling.scopeCatching
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.net.InetAddress
import io.ktor.serialization.kotlinx.json.json
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

actual object PlatformModule {

    private const val NAME = "AndroidPlatformModule"

    actual val di = DI.Module(NAME) {
        bindSingleton<OkHttpClient> {
            val ssl = instance<SSL>()

            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .apply {
                    ssl.socketFactory?.let {
                        sslSocketFactory(it, ssl.trustManager)
                        socketFactory(it)
                    }
                }
                .build()
        }
        bindSingleton<Dns> {
            DnsOverHttps.Builder()
                .client(instance())
                .url("https://dns.google/dns-query".toHttpUrl())
                .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
                .build()
        }
        bindSingleton<SSL> {
            SSL()
        }
        bindSingleton<HttpClient> {
            val ssl = instance<SSL>()

            HttpClient(OkHttp) {
                followRedirects = true
                engine {
                    config {
                        hostnameVerifier { _, _ -> true }
                        ssl.socketFactory?.let {
                            sslSocketFactory(it, ssl.trustManager)
                        }
                    }
                }
                install(ContentNegotiation) {
                    json(instance(), ContentType.Application.Json)
                    json(instance(), ContentType.Text.Plain)
                }
                install(HttpCache)
            }
        }
        bindSingleton<HttpClient>(tag = NetworkModule.FALLBACK_ENGINE_TAG) {
            val ssl = instance<SSL>()

            HttpClient(Android) {
                followRedirects = true
                engine {
                    sslManager = { httpsURLConnection ->
                        httpsURLConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
                        ssl.socketFactory?.let {
                            httpsURLConnection.sslSocketFactory = it
                        }
                    }
                }
                install(ContentNegotiation) {
                    json(instance(), ContentType.Application.Json)
                    json(instance(), ContentType.Text.Plain)
                }
                install(HttpCache)
            }
        }
        bindSingleton<HttpClient>(tag = NetworkModule.FALLBACK_CLIENT_TAG) {
            val ssl = instance<SSL>()

            HttpClient(OkHttp) {
                followRedirects = true
                engine {
                    config {
                        followRedirects(true)
                        followSslRedirects(true)
                        dns(instance())

                        hostnameVerifier { _, _ -> true }
                        ssl.socketFactory?.let {
                            sslSocketFactory(it, ssl.trustManager)
                        }
                    }
                }
                install(ContentNegotiation) {
                    json(instance(), ContentType.Application.Json)
                    json(instance(), ContentType.Text.Plain)
                }
                install(HttpCache)
            }
        }
        bindSingleton<FirebaseWrapper.Creator> {
            if (AppInitializer.isSekretLoaded(instance())) {
                FirebaseWrapper.Creator.Available(FirebaseWrapper())
            } else {
                FirebaseWrapper.Creator.Empty
            }
        }
    }

    private data class SSL(
        val sslContext: SSLContext? = scopeCatching {
            SSLContext.getInstance("TLS")
        }.getOrNull() ?: scopeCatching {
            SSLContext.getInstance("SSL")
        }.getOrNull(),
        val trustManager: X509TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<out X509Certificate?>?,
                authType: String?
            ) { }

            override fun checkServerTrusted(
                chain: Array<out X509Certificate?>?,
                authType: String?
            ) { }

            override fun getAcceptedIssuers(): Array<out X509Certificate?>? = arrayOf()
        },
        val socketFactory: SSLSocketFactory? = scopeCatching {
            sslContext?.init(null, arrayOf(trustManager), SecureRandom())
            sslContext?.socketFactory
        }.getOrNull()
    )
}