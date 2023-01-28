package dev.datlag.burningseries

import androidx.multidex.MultiDexApplication
import dev.datlag.burningseries.module.NetworkModule
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton

class App : MultiDexApplication(), DIAware {

    override val di: DI = DI {
        bindSingleton {
            applicationContext
        }

        import(NetworkModule.di)
    }
}