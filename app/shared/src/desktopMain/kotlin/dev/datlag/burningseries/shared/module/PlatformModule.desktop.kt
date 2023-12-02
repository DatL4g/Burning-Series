package dev.datlag.burningseries.shared.module

import android.content.Context
import dev.datlag.burningseries.database.DriverFactory
import dev.datlag.burningseries.shared.AppIO
import dev.datlag.burningseries.shared.Sekret
import dev.datlag.burningseries.shared.getPackageName
import dev.datlag.burningseries.shared.other.StateSaver
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

actual object PlatformModule {

    private const val NAME = "PlatformModuleDesktop"

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
        bindSingleton("BurningSeriesDBFile") {
            AppIO.getFileInUserDataDir("bs.db")
        }
        bindSingleton {
            DriverFactory(instance("BurningSeriesDBFile"))
        }
        if (StateSaver.sekretLibraryLoaded) {
            bindEagerSingleton {
                Firebase.initialize(
                    context = Context(),
                    options = FirebaseOptions(
                        applicationId = Sekret().firebaseApplication(getPackageName())!!,
                        apiKey = Sekret().firebaseApiKey(getPackageName())!!,
                        projectId = Sekret().firebaseProject(getPackageName())
                    )
                )
            }
            bindEagerSingleton {
                val store = Firebase.firestore(instance())

                store.setSettings(
                    persistenceEnabled = false,
                    // sslEnabled = false, // requires non-default host (firebase.googleapis.com)
                )
                store
            }
        }
    }

}