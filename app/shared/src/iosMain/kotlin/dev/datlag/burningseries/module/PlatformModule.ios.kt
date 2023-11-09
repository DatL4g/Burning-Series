package dev.datlag.burningseries.module

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import org.kodein.di.DI
import org.kodein.di.bindSingleton

actual object PlatformModule {

    private const val NAME = "PlatformModuleIOS"

    actual val di: DI.Module = DI.Module(NAME) {
        bindSingleton {
            HttpClient(Darwin) {
                engine {
                    configureRequest {
                        setAllowsCellularAccess(true)
                    }
                }
            }
        }
    }

}