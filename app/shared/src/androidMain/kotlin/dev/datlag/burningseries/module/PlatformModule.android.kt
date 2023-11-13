package dev.datlag.burningseries.module

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import java.util.concurrent.TimeUnit

actual object PlatformModule {

    private const val NAME = "PlatformModuleAndroid"

    actual val di: DI.Module = DI.Module(NAME) {
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
            }
        }
    }

}