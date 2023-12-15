package dev.datlag.burningseries.shared.module

import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.decode.GifDecoder
import coil3.decode.ImageDecoderDecoder
import coil3.decode.SvgDecoder
import coil3.disk.DiskCache
import coil3.fetch.NetworkFetcher
import coil3.memory.MemoryCache
import coil3.request.allowHardware
import coil3.request.crossfade
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.shared.Sekret
import dev.datlag.burningseries.shared.getPackageName
import dev.datlag.burningseries.shared.other.StateSaver
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import kotlinx.serialization.json.Json
import okio.FileSystem
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

actual object PlatformModule {

    private const val NAME = "PlatformModuleAndroid"

    @OptIn(ExperimentalCoilApi::class)
    actual val di: DI.Module = DI.Module(NAME) {
        bindSingleton {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
        bindSingleton {
            HttpClient(OkHttp) {
                engine {
                    config {
                        followRedirects(true)
                        connectTimeout(3, TimeUnit.MINUTES)
                        readTimeout(3, TimeUnit.MINUTES)
                        writeTimeout(3, TimeUnit.MINUTES)
                    }
                }
                install(ContentNegotiation) {
                    json(instance(), ContentType.Application.Json)
                    json(instance(), ContentType.Text.Plain)
                }
            }
        }
        bindSingleton {
            DriverFactory(instance())
        }
        if (StateSaver.sekretLibraryLoaded) {
            bindEagerSingleton {
                AppConfiguration.create(Sekret().mongoApplication(getPackageName())!!)
            }
            bindEagerSingleton {
                Firebase.initialize(
                    context = instance<Context>(),
                    options = FirebaseOptions(
                        applicationId = Sekret().firebaseApplication(getPackageName())!!,
                        apiKey = Sekret().firebaseApiKey(getPackageName())!!,
                        projectId = Sekret().firebaseProject(getPackageName())
                    )
                )
            }
        }
        bindSingleton {
            ImageLoader.Builder(instance())
                .components {
                    add(NetworkFetcher.Factory(lazyOf(instance<HttpClient>())))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                    add(SvgDecoder.Factory())
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(instance())
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
                        .maxSizeBytes(512L * 1024 * 1024) // 512MB
                        .build()
                }
                .allowHardware(false)
                .crossfade(true)
                .build()
        }
    }

}