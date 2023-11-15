package dev.datlag.burningseries.module

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfitBuilder
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.state.EpisodeStateMachine
import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.network.state.SearchStateMachine
import io.ktor.client.*
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

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
        bindSingleton {
            EpisodeStateMachine(instance())
        }
        bindSingleton {
            App.create("").also {
                GlobalScope.launchIO {
                    it.login(Credentials.anonymous())
                }
            }
        }
    }
}