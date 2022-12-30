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
import androidx.compose.ui.window.singleWindowApplication
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.datlag.burningseries.module.DataStoreModule
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.module.PlatformModule
import dev.datlag.burningseries.other.AppIO
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.other.StringRes
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import javax.swing.SwingUtilities

val LocalWindow = compositionLocalOf<ComposeWindow> { error("No window state provided") }
var keyEventListener: ((KeyEvent) -> Boolean)? = null

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val windowState = WindowState()
    val lifecycle = LifecycleRegistry()
    val di = DI {
        import(NetworkModule.di)
    }

    val root = NavHostComponent.create(DefaultComponentContext(lifecycle), di)
    val resources = Resources()
    val stringRes = StringRes.create(resources)

    singleWindowApplication(
        state = windowState,
        title = stringRes.appName,
        onKeyEvent = {
            keyEventListener?.invoke(it) ?: false
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

            App {
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