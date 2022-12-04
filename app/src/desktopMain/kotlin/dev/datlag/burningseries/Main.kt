package dev.datlag.burningseries

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.datlag.burningseries.module.DataStoreModule
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.module.PlatformModule
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.other.StringRes
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import javax.swing.SwingUtilities

fun main() {
    val windowState = WindowState()
    val lifecycle = LifecycleRegistry()
    val di = DI {
        import(NetworkModule.di)
        import(DataStoreModule.di)
    }

    val root = NavHostComponent.create(DefaultComponentContext(lifecycle), di)
    val resources = Resources()
    val stringRes = StringRes.create(resources)

    singleWindowApplication(
        state = windowState,
        title = stringRes.appName
    ) {
        LifecycleController(lifecycle, windowState)

        CompositionLocalProvider(
            LocalResources provides resources,
            LocalStringRes provides stringRes,
            LocalOrientation provides Orientation.LANDSCAPE
        ) {
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