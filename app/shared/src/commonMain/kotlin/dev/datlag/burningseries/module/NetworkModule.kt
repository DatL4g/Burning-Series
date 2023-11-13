package dev.datlag.burningseries.module

import dev.datlag.burningseries.network.state.HomeStateMachine
import dev.datlag.burningseries.network.state.SearchStateMachine
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

object NetworkModule {

    const val NAME = "NetworkModule"

    val di = DI.Module(NAME) {
        import(PlatformModule.di)

        bindSingleton {
            HomeStateMachine(instance())
        }
        bindSingleton {
            SearchStateMachine(instance())
        }
    }
}