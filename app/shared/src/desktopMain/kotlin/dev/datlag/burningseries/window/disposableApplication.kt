package dev.datlag.burningseries.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.*
import kotlin.system.exitProcess

fun disposableApplication(
    exitProcessOnExit: Boolean = true,
    onExitProcess: (() -> Unit)? = null,
    content: @Composable ApplicationScope.() -> Unit
) {
    var restartApplication = true

    while (restartApplication) {
        restartApplication = newApplication(content)
    }

    if (exitProcessOnExit) {
        onExitProcess?.invoke()
        exitProcess(0)
    }
}

fun disposableSingleWindowApplication(
    state: WindowState = WindowState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    exitProcessOnExit: Boolean = true,
    onExitProcess: () -> Unit = {  },
    content: @Composable FrameWindowScope.() -> Unit
) = disposableApplication(exitProcessOnExit, onExitProcess) {
    Window(
        onCloseRequest = this::exitApplication,
        state = state,
        visible = visible,
        title = title,
        icon = icon,
        undecorated = undecorated,
        transparent = transparent,
        resizable = resizable,
        enabled = enabled,
        focusable = focusable,
        alwaysOnTop = alwaysOnTop,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        content = content
    )
}

private fun newApplication(content: @Composable ApplicationScope.() -> Unit): Boolean {
    var shouldRestart = false

    application(exitProcessOnExit = false) {
        val applicationDisposer = remember(this) {
            ApplicationDisposerImpl { restart ->
                shouldRestart = restart
                exitApplication()
            }
        }

        CompositionLocalProvider(LocalApplicationDisposer provides applicationDisposer) {
            val disposer = LocalApplicationDisposer.current

            val disposerApplicationScope = remember(disposer) {
                object : ApplicationScope {
                    override fun exitApplication() = disposer.exit()
                }
            }

            disposerApplicationScope.content()
        }
    }

    return shouldRestart
}

private class ApplicationDisposerImpl(private val onExit: (restart: Boolean) -> Unit) : ApplicationDisposer {

    @Volatile
    private var isAlive: Boolean = true

    override fun exit() = exitApplication(false)

    override fun restart() = exitApplication(true)

    private fun exitApplication(restart: Boolean) {
        check(isAlive) { "Application is no longer alive." }
        isAlive = false
        onExit(restart)
    }
}