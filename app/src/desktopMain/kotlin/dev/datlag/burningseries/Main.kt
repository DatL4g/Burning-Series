package dev.datlag.burningseries

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.sun.javafx.application.PlatformImpl
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.isStarted
import dev.datlag.burningseries.common.systemProperty
import dev.datlag.burningseries.datastore.common.loggingMode
import dev.datlag.burningseries.datastore.preferences.AppSettings
import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.other.AppIO
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.other.StringRes
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import javafx.application.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File
import java.net.CookieHandler
import javax.swing.SwingUtilities

val LocalWindow = compositionLocalOf<ComposeWindow> { error("No window state provided") }
var keyEventListener: ((KeyEvent) -> Boolean)? = null
private var defaultAllowRestrictedHeaders: String? = null

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val windowState = WindowState()
    val lifecycle = LifecycleRegistry()
    val di = DI {
        import(NetworkModule.di)
    }
    defaultAllowRestrictedHeaders = systemProperty("sun.net.http.allowRestrictedHeaders", "true")

    val root = NavHostComponent.create(DefaultComponentContext(lifecycle), di)
    val resources = Resources()
    val stringRes = StringRes.create(resources)

    val finishListener = object : PlatformImpl.FinishListener {
        override fun exitCalled() { }
        override fun idle(implicitExit: Boolean) { }
    }

    singleWindowApplication(
        state = windowState,
        title = stringRes.appName,
        onKeyEvent = {
            keyEventListener?.invoke(it) ?: false
        },
        exitProcessOnExit = true,
        onCloseRequest = {
            PlatformImpl.removeListener(finishListener)

            defaultAllowRestrictedHeaders?.ifBlank { null }?.let {
                systemProperty("sun.net.http.allowRestrictedHeaders", it)
            }

            false
        },
        onCreateApplication = {
            PlatformImpl.addListener(finishListener)
            CookieHandler.setDefault(null)

            val scope = rememberCoroutineScope()
            val actionLogger: ActionLogger by di.instance()
            val appSettings: DataStore<AppSettings> by di.instance()

            try {
                Platform.startup {
                    scope.launch(Dispatchers.IO) {
                        delay(10000)
                        withContext(Dispatchers.Main) {
                            isStarted = true
                        }
                    }
                }
            } catch (ignored: Throwable) {
                scope.launch(Dispatchers.IO) {
                    delay(10000)
                    withContext(Dispatchers.Main) {
                        isStarted = true
                    }
                }
            }

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

            scope.launch(Dispatchers.IO) {
                actionLogger.reset(appSettings.loggingMode.getValueBlocking(appSettings.loggingMode.first()))
            }
        }
    ) {
        val scope = rememberCoroutineScope()
        LifecycleController(lifecycle, windowState)

        AppIO.loadAppIcon(
            this.window,
            resources,
            scope
        )

        CompositionLocalProvider(
            LocalWindow provides this.window,
            LocalResources provides resources,
            LocalStringRes provides stringRes,
            LocalOrientation provides Orientation.LANDSCAPE
        ) {
            keyEventListener = {
                when (it.key) {
                    Key.Escape -> {
                        window.placement = WindowPlacement.Floating
                        true
                    }
                    Key.F11 -> {
                        window.placement = WindowPlacement.Fullscreen
                        true
                    }
                    else -> false
                }
            }

            App(di) {
                root.render()
            }
        }
    }
}

private inline fun <T : Any> runOnMainThreadBlocking(crossinline block: () -> T): T {
    lateinit var result: T
    SwingUtilities.invokeAndWait { result = block() }
    return result
}