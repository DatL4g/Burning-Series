package dev.datlag.burningseries.module

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import dev.datlag.burningseries.common.nullableFirebaseInstance
import dev.datlag.burningseries.network.EpisodeStateMachine
import dev.datlag.burningseries.network.HomeStateMachine
import dev.datlag.burningseries.network.SaveStateMachine
import dev.datlag.burningseries.network.SearchStateMachine
import dev.datlag.burningseries.network.SeriesStateMachine
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import okio.FileSystem
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

data object NetworkModule {

    const val NAME = "NetworkModule"

    val di = DI.Module(NAME) {
        import(PlatformModule.di)

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
    }
}