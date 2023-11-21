package dev.datlag.burningseries.module

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfitBuilder
import dev.datlag.burningseries.Sekret
import dev.datlag.burningseries.getPackageName
import dev.datlag.burningseries.network.Firestore
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.state.EpisodeStateMachine
import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.network.state.SaveStateMachine
import dev.datlag.burningseries.network.state.SearchStateMachine
import dev.datlag.burningseries.other.StateSaver
import io.ktor.client.*
import io.realm.kotlin.mongodb.App
import org.kodein.di.*

object NetworkModule {

    private const val TAG_KTORFIT_JSONBASE = "JsonBaseKtorfit"
    private const val TAG_KTORFIT_FIRESTORE = "FirestoreKtorfit"
    const val NAME = "NetworkModule"

    val di = DI.Module(NAME) {
        import(DatabaseModule.di)

        bindSingleton {
            ktorfitBuilder {
                httpClient(instance<HttpClient>())
            }
        }
        bindSingleton(TAG_KTORFIT_JSONBASE) {
            val builder = instance<Ktorfit.Builder>()
            builder.build {
                baseUrl("https://jsonbase.com/")
            }
        }
        bindSingleton {
            val jsonBaseKtor: Ktorfit = instance(TAG_KTORFIT_JSONBASE)
            jsonBaseKtor.create<JsonBase>()
        }
        bindSingleton {
            HomeStateMachine(instance())
        }
        bindSingleton {
            SearchStateMachine(instance())
        }
        if (StateSaver.sekretLibraryLoaded) {
            bindEagerSingleton {
                App.create(Sekret().mongoApplication(getPackageName())!!)
            }
            bindEagerSingleton(TAG_KTORFIT_FIRESTORE) {
                val builder = instance<Ktorfit.Builder>()
                builder.build {
                    baseUrl("https://firestore.googleapis.com/v1/projects/${Sekret().firebaseProject(getPackageName())!!}/")
                }
            }
            bindEagerSingleton {
                val firestoreKtor: Ktorfit = instance(TAG_KTORFIT_FIRESTORE)
                firestoreKtor.create<Firestore>()
            }
        }
        bindProvider {
            EpisodeStateMachine(instance(), instance(), instanceOrNull(), instanceOrNull(), instanceOrNull())
        }
        bindEagerSingleton {
            SaveStateMachine(instance(), instance(), instanceOrNull(), instanceOrNull(), instanceOrNull())
        }
    }
}