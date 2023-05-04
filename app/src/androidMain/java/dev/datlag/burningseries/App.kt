package dev.datlag.burningseries

import androidx.datastore.core.DataStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.multidex.MultiDexApplication
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.systemProperty
import dev.datlag.burningseries.datastore.common.loggingMode
import dev.datlag.burningseries.datastore.preferences.AppSettings
import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.module.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.io.File

class App : MultiDexApplication(), DIAware, DefaultLifecycleObserver {

    private var defaultAllowRestrictedHeaders: String? = null

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        defaultAllowRestrictedHeaders = systemProperty("sun.net.http.allowRestrictedHeaders", "true")

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            val logFile: File by di.instance("ErrorFile")

            runCatching {
                val writer = logFile.printWriter()
                writer.write(String())
                e.printStackTrace(writer)

                runCatching {
                    writer.flush()
                    writer.close()
                }.getOrNull()

                logFile.setLastModified(Clock.System.now().toEpochMilliseconds())
            }.getOrNull()
        }

        val actionLogger: ActionLogger by di.instance()
        val appSettings: DataStore<AppSettings> by di.instance()
        GlobalScope.launch(Dispatchers.IO) {
            actionLogger.reset(appSettings.loggingMode.getValueBlocking(appSettings.loggingMode.first()))
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        defaultAllowRestrictedHeaders?.ifBlank { null }?.let {
            systemProperty("sun.net.http.allowRestrictedHeaders", it)
        }
    }

    override val di: DI = DI {
        bindSingleton {
            applicationContext
        }

        import(NetworkModule.di)
    }
}