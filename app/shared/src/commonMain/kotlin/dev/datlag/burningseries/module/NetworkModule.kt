package dev.datlag.burningseries.module

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfitBuilder
import dev.datlag.burningseries.Sekret
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.getPackageName
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.state.EpisodeStateMachine
import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.network.state.SearchStateMachine
import dev.datlag.burningseries.other.StateSaver
import io.ktor.client.*
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.kodein.di.*

object NetworkModule {

    private const val TAG_KTORFIT_JSONBASE = "JsonBaseKtorfit"
    const val NAME = "NetworkModule"

    @OptIn(DelicateCoroutinesApi::class)
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
        }
        bindEagerSingleton {
            EpisodeStateMachine(instance(), instanceOrNull())
        }
    }
}