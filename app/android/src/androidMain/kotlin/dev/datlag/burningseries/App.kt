package dev.datlag.burningseries

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.multidex.MultiDexApplication
import dev.datlag.burningseries.model.common.systemProperty
import dev.datlag.burningseries.network.state.NetworkStateSaver
import dev.datlag.burningseries.shared.module.NetworkModule
import dev.datlag.burningseries.shared.other.StateSaver
import dev.datlag.sekret.NativeLoader
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton

class App : MultiDexApplication(), DIAware, DefaultLifecycleObserver {

    private var defaultAllowRestrictedHeaders: String? = null

    override val di: DI by lazy {
        DI {
            bindSingleton {
                applicationContext
            }
            bindSingleton("APP_VERSION") {
                BuildConfig.VERSION_NAME
            }

            import(NetworkModule.di)
        }
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }
        StateSaver.sekretLibraryLoaded = NativeLoader.loadLibrary("sekret")
        defaultAllowRestrictedHeaders = systemProperty("sun.net.http.allowRestrictedHeaders", "true")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        defaultAllowRestrictedHeaders?.ifBlank { null }?.let {
            systemProperty("sun.net.http.allowRestrictedHeaders", it)
        }
        runBlocking {
            NetworkStateSaver.firebaseUser?.delete()
        }
    }
}