package dev.datlag.burningseries.module

import dev.datlag.burningseries.AppIO
import dev.datlag.burningseries.database.DriverFactory
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

actual object PlatformModule {

    private const val NAME = "PlatformModuleDesktop"

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
        bindSingleton("BurningSeriesDBFile") {
            AppIO.getFileInUserDataDir("bs.db")
        }
        bindSingleton {
            DriverFactory(instance("BurningSeriesDBFile"))
        }
    }

}