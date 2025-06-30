package dev.datlag.mimasu.extension.module

import dev.datlag.mimasu.extension.AppInitializer
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import io.ktor.client.HttpClient
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

actual object PlatformModule {

    private const val NAME = "AndroidPlatformModule"

    actual val di = DI.Module(NAME) {
        bindSingleton<OkHttpClient> {
            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }
        bindSingleton<Dns> {
            DnsOverHttps.Builder()
                .client(instance())
                .url("https://dns.google/dns-query".toHttpUrl())
                .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
                .build()
        }
        bindSingleton<HttpClient> {
            HttpClient(OkHttp) {
                followRedirects = true
                install(ContentNegotiation) {
                    json(instance(), ContentType.Application.Json)
                    json(instance(), ContentType.Text.Plain)
                }
                install(HttpCache)
            }
        }
        bindSingleton<HttpClient>(tag = NetworkModule.FALLBACK_CLIENT_TAG) {
            HttpClient(OkHttp) {
                followRedirects = true
                engine {
                    config {
                        followRedirects(true)
                        followSslRedirects(true)
                        dns(instance())
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
}