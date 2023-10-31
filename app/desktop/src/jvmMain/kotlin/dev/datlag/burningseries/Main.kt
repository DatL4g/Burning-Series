package dev.datlag.burningseries

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.datlag.burningseries.common.lifecycle.LocalLifecycleOwner
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import dev.datlag.burningseries.window.disposableSingleWindowApplication
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.kodein.di.DI

fun main(vararg args: String) {
    runWindow()
}

@OptIn(ExperimentalDecomposeApi::class)
private fun runWindow() {
    val appTitle = StringDesc.Resource(SharedRes.strings.app_name).localized()
    AppIO.applyTitle(appTitle)
    Napier.base(DebugAntilog())

    val windowState = WindowState()
    val lifecycle = LifecycleRegistry()
    val lifecycleOwner = object : LifecycleOwner {
        override val lifecycle: Lifecycle = lifecycle
    }
    val di = DI {
        import(NetworkModule.di)
    }
    val root = NavHostComponent(
        componentContext = DefaultComponentContext(lifecycle),
        di = di
    )

    disposableSingleWindowApplication(
        state = windowState,
        title = appTitle,
        onKeyEvent = {
            false
        },
        exitProcessOnExit = true
    ) {
        LifecycleController(lifecycle, windowState)

        CompositionLocalProvider(
            LocalLifecycleOwner provides lifecycleOwner,
            LocalWindow provides this.window
        ) {
            App(di) {
                root.render()
            }
        }
    }
}