package dev.datlag.burningseries.shared.module

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfitBuilder
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.common.toGenres
import dev.datlag.burningseries.database.common.toSearchItems
import dev.datlag.burningseries.network.Firestore
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.WrapAPI
import dev.datlag.burningseries.network.realm.RealmLoader
import dev.datlag.burningseries.network.state.EpisodeStateMachine
import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.network.state.SaveStateMachine
import dev.datlag.burningseries.network.state.SearchStateMachine
import dev.datlag.burningseries.shared.Sekret
import dev.datlag.burningseries.shared.getPackageName
import dev.datlag.burningseries.shared.other.StateSaver
import io.ktor.client.*
import io.realm.kotlin.mongodb.App
import org.kodein.di.*

object NetworkModule {

    private const val TAG_KTORFIT_JSONBASE = "JsonBaseKtorfit"
    private const val TAG_KTORFIT_WRAPAPI = "WrapAPIKtorfit"
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
        bindSingleton(TAG_KTORFIT_WRAPAPI) {
            val builder = instance<Ktorfit.Builder>()
            builder.build {
                baseUrl("https://wrapapi.com/use/")
            }
        }
        bindSingleton {
            val wrapApiKtor: Ktorfit = instance(TAG_KTORFIT_WRAPAPI)
            wrapApiKtor.create<WrapAPI>()
        }
        bindSingleton {
            HomeStateMachine(
                client = instance(),
                json = instance(),
                wrapApi = instance(),
                wrapApiKey = if (StateSaver.sekretLibraryLoaded) {
                    Sekret().wrapApi(getPackageName())
                } else { null }
            )
        }
        bindSingleton {
            val database = instance<BurningSeries>()

            SearchStateMachine(
                client = instance(),
                json = instance(),
                wrapApi = instance(),
                wrapApiKey = if (StateSaver.sekretLibraryLoaded) {
                    Sekret().wrapApi(getPackageName())
                } else { null },
                saveToDB = {
                    database.burningSeriesQueries.transaction {
                        it.genres.forEach { genre ->
                            genre.toSearchItems().forEach { item ->
                                database.burningSeriesQueries.insertSearchItem(item)
                            }
                        }
                    }
                },
                loadFromDB = {
                    database.burningSeriesQueries.selectAllSearchItems().executeAsList().toGenres()
                }
            )
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
        bindEagerSingleton {
            RealmLoader(instanceOrNull())
        }
        bindProvider {
            EpisodeStateMachine(instance(), instance(), instance(), instanceOrNull(), instanceOrNull())
        }
        bindEagerSingleton {
            SaveStateMachine(instance(), instance(), instance(), instanceOrNull(), instanceOrNull())
        }
    }
}