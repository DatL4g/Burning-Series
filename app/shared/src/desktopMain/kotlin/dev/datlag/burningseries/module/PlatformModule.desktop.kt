package dev.datlag.burningseries.module

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import org.kodein.di.DI
import org.kodein.di.bindSingleton

actual object PlatformModule {

    private const val NAME = "PlatformModuleDesktop"

    actual val di: DI.Module = DI.Module(NAME) {
        bindSingleton {
            HttpClient(OkHttp) {
                engine {
                    config {
                        followRedirects(true)
                    }
                }
            }
        }
    }

}