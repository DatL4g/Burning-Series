package dev.datlag.burningseries.module

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import coil3.ImageLoader
import coil3.PlatformContext
import dev.datlag.burningseries.BuildKonfig
import dev.datlag.burningseries.Sekret
import dev.datlag.burningseries.common.deviceName
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.firebase.FirebaseFactory
import dev.datlag.burningseries.firebase.initialize
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.settings.DataStoreAppSettings
import dev.datlag.burningseries.settings.DataStoreUserSettings
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.AppSettings
import dev.datlag.burningseries.settings.model.UserSettings
import dev.datlag.tooling.Platform
import dev.datlag.tooling.Tooling
import dev.datlag.tooling.createAsFileSafely
import dev.datlag.tooling.getRWUserConfigFile
import dev.datlag.tooling.getRWUserDataFile
import dev.datlag.tooling.systemProperty
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.JvmCodeAuthFlowFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit

actual object PlatformModule {

    private const val NAME = "DesktopPlatformModule"
    private const val APP_NAME = "Burning-Series"
    private const val APP_VERSION = "APP_VERSION"
    private const val DEVICE_NAME = "DEVICE_NAME"

    actual val di: DI.Module = DI.Module(NAME) {
        bindSingleton<PlatformContext> {
            PlatformContext.INSTANCE
        }
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
                followRedirects = true
                engine {
                    config {
                        followRedirects(true)
                        followSslRedirects(true)
                        connectTimeout(3, TimeUnit.MINUTES)
                        readTimeout(3, TimeUnit.MINUTES)
                        writeTimeout(3, TimeUnit.MINUTES)
                        dns(instance())
                    }
                }
                install(ContentNegotiation) {
                    json(instance(), ContentType.Application.Json)
                    json(instance(), ContentType.Text.Plain)
                }
            }
        }
        bindSingleton<HttpClient>("STREAM_CLIENT") {
            HttpClient(OkHttp) {
                followRedirects = true
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
        bindSingleton<DataStore<UserSettings>> {
            DataStoreFactory.create(
                storage = OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = UserSettings.SettingsSerializer,
                    producePath = {
                        Tooling.getRWUserConfigFile(
                            child = "user.settings",
                            appName = APP_NAME,
                            appVersion = "v6"
                        ).also { it.createAsFileSafely() }.toOkioPath()
                    }
                )
            )
        }
        bindSingleton<Settings.PlatformUserSettings> {
            DataStoreUserSettings(instance())
        }
        bindSingleton<FirebaseFactory> {
            if (StateSaver.sekretLibraryLoaded) {
                FirebaseFactory.initialize(
                    projectId = Sekret.firebaseProject(BuildKonfig.packageName),
                    applicationId = Sekret.firebaseApplication(BuildKonfig.packageName)!!,
                    apiKey = Sekret.firebaseApiKey(BuildKonfig.packageName)!!,
                    localLogger = object : FirebaseFactory.Crashlytics.LocalLogger {
                        override fun warn(message: String?) {
                            message?.let { Napier.w(it) }
                        }

                        override fun error(message: String?) {
                            message?.let { Napier.e(it) }
                        }

                        override fun error(throwable: Throwable?) {
                            throwable?.let { Napier.e("", it) }
                        }
                    }
                )
            } else {
                FirebaseFactory.Empty
            }
        }
        bindSingleton<DriverFactory> {
            DriverFactory(
                file = Tooling.getRWUserDataFile(
                    child = "bs.db",
                    appName = APP_NAME,
                    appVersion = "v6"
                )
            )
        }
        bindSingleton<CodeAuthFlowFactory> {
            JvmCodeAuthFlowFactory()
        }
        systemProperty("jpackage.app-version")?.let {
            bindSingleton(APP_VERSION) { it }
        }
        bindSingleton<String>(DEVICE_NAME) {
            Platform.deviceName()
        }
    }
}

actual fun ImageLoader.Builder.extendImageLoader(): ImageLoader.Builder = this