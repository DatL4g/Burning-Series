package dev.datlag.mimasu.extension

import android.content.Context
import androidx.multidex.MultiDexApplication
import dev.datlag.mimasu.extension.module.NetworkModule
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton

class App : MultiDexApplication(), DIAware {

    override val di: DI = DI {
        bindSingleton<Context> {
            applicationContext
        }

        import(NetworkModule.di)
    }

    override fun onCreate() {
        super.onCreate()
    }
}