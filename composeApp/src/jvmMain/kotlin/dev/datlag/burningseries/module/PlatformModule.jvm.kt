package dev.datlag.burningseries.module

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import dev.datlag.burningseries.settings.DataStoreAppSettings
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.AppSettings
import dev.datlag.tooling.Tooling
import dev.datlag.tooling.createAsFileSafely
import dev.datlag.tooling.getRWUserConfigFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.net.InetAddress
import java.util.concurrent.TimeUnit

actual object PlatformModule {

    private const val NAME = "DesktopPlatformModule"
    private const val APP_NAME = "Burning-Series"

    actual val di: DI.Module = DI.Module(NAME) {
        bindSingleton<OkHttpClient> {
            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .writeTimeout(3, TimeUnit.MINUTES)
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
                engine {
                    config {
                        followRedirects(true)
                        connectTimeout(3, TimeUnit.MINUTES)
                        readTimeout(3, TimeUnit.MINUTES)
                        writeTimeout(3, TimeUnit.MINUTES)
                        dns(instance())
                    }
                }
                /*install(ContentNegotiation) {
                    json(instance(), ContentType.Application.Json)
                    json(instance(), ContentType.Text.Plain)
                }*/
            }
        }
        bindSingleton<DataStore<AppSettings>> {
            DataStoreFactory.create(
                storage = OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = AppSettings.SettingsSerializer,
                    producePath = {
                        Tooling.getRWUserConfigFile(
                            child = "app.settings",
                            appName = APP_NAME,
                            appVersion = "v6"
                        ).also { it.createAsFileSafely() }.toOkioPath()
                    }
                )
            )
        }
        bindSingleton<Settings.PlatformAppSettings> {
            DataStoreAppSettings(instance())
        }
    }
}