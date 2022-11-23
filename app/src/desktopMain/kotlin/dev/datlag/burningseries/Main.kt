package dev.datlag.burningseries

import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import javax.swing.SwingUtilities

fun main() {
    val windowState = WindowState()
    val lifecycle = LifecycleRegistry()
    val root = runOnMainThreadBlocking {
        NavHostComponent(DefaultComponentContext(lifecycle))
    }

    singleWindowApplication(
        state = windowState,
        title = "Burning-Series"
    ) {
        LifecycleController(lifecycle, windowState)

        App {
            root.render()
        }
    }
}

private inline fun <T : Any> runOnMainThreadBlocking(crossinline block: () -> T): T {
    lateinit var result: T
    SwingUtilities.invokeAndWait { result = block() }
    return result
}