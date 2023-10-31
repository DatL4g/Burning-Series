package dev.datlag.burningseries

import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.window.disposableSingleWindowApplication
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun main(vararg args: String) {
    runWindow()
}

private fun runWindow() {
    val appTitle = StringDesc.Resource(SharedRes.strings.app_name).localized()
    AppIO.applyTitle(appTitle)
    Napier.base(DebugAntilog())

    val windowState = WindowState()
    val lifecycle = LifecycleRegistry()

    disposableSingleWindowApplication(
        state = windowState,
        title = appTitle,
        onKeyEvent = {
            false
        },
        exitProcessOnExit = true
    ) {
        LifecycleController(lifecycle, windowState)


    }
}