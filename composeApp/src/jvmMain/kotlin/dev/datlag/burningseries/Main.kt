package dev.datlag.burningseries

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureIcon
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.datlag.burningseries.common.nullableFirebaseInstance
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.app_name
import dev.datlag.burningseries.firebase.FirebaseFactory
import dev.datlag.burningseries.firebase.initializePlatform
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.ui.navigation.RootComponent
import dev.datlag.kast.Kast
import dev.datlag.sekret.NativeLoader
import dev.datlag.tooling.Tooling
import dev.datlag.tooling.applicationTitle
import dev.datlag.tooling.decompose.lifecycle.LocalLifecycleOwner
import dev.datlag.tooling.scopeCatching
import dev.datlag.tooling.systemProperty
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import javax.swing.SwingUtilities
import java.io.File

val LocalWindow = staticCompositionLocalOf<ComposeWindow?> { null }

fun main(vararg args: String) {
    Napier.base(DebugAntilog())
    StateSaver.sekretLibraryLoaded = NativeLoader.loadLibrary(
        name = "sekret",
        path = systemProperty("compose.application.resources.dir")?.let(::File)
    )
    FirebaseFactory.initializePlatform()
    Kast.restartDiscovery()

    val di = DI {
        import(NetworkModule.di)
    }
    val firebase = di.nullableFirebaseInstance()?.auth

    Runtime.getRuntime().addShutdownHook(Thread {
        Kast.dispose()

        runBlocking {
            firebase?.delete()
        }
    })

    runWindow(di)

    Kast.dispose()
    runBlocking {
        firebase?.delete()
    }
}

@OptIn(ExperimentalDecomposeApi::class, DelicateCoilApi::class)
private fun runWindow(di: DI) {
    val appTitle = runBlocking {
        getString(Res.string.app_name)
    }
    Tooling.applicationTitle(appTitle)

    val imageLoader by di.instance<ImageLoader>()
    SingletonImageLoader.setUnsafe(imageLoader)

    val windowState = WindowState()
    val lifecycle = LifecycleRegistry()
    val lifecycleOwner = object : LifecycleOwner {
        override val lifecycle: Lifecycle = lifecycle
    }
    val backDispatcher = BackDispatcher()
    val root = runOnUiThread {
        RootComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                backHandler = backDispatcher
            ),
            di = di
        )
    }

    singleWindowApplication(
        state = windowState,
        title = appTitle,
        exitProcessOnExit = true
    ) {
        LifecycleController(lifecycle, windowState)
        App.applyIcon(
            this.window,
            rememberCoroutineScope(),
            MokoRes.assets.icns.launcher_icns,
            MokoRes.assets.ico.launcher_128_ico,
            MokoRes.assets.ico.launcher_96_ico,
            MokoRes.assets.ico.launcher_64_ico,
            MokoRes.assets.ico.launcher_48_ico,
            MokoRes.assets.ico.launcher_32_ico,
            MokoRes.assets.ico.launcher_16_ico,
            MokoRes.assets.png.launcher_128_png,
            MokoRes.assets.png.launcher_96_png,
            MokoRes.assets.png.launcher_64_png,
            MokoRes.assets.png.launcher_48_png,
            MokoRes.assets.png.launcher_32_png,
            MokoRes.assets.png.launcher_16_png,
            MokoRes.assets.svg.launcher_128_svg,
            MokoRes.assets.svg.launcher_96_svg,
            MokoRes.assets.svg.launcher_64_svg,
            MokoRes.assets.svg.launcher_48_svg,
            MokoRes.assets.svg.launcher_32_svg,
            MokoRes.assets.svg.launcher_16_svg
        )

        val appSettings by di.instance<Settings.PlatformAppSettings>()
        LaunchedEffect(Unit) {
            appSettings.increaseStartCounter()
        }

        CompositionLocalProvider(
            LocalLifecycleOwner provides lifecycleOwner,
            LocalWindow provides window
        ) {
            App(di) {
                PredictiveBackGestureOverlay(
                    backDispatcher = backDispatcher,
                    backIcon = { progress, _ ->
                        PredictiveBackGestureIcon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            progress = progress,
                            iconTintColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    root.render()
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        val res = scopeCatching(block)
        error = res.exceptionOrNull()
        result = res.getOrNull()
    }

    error?.also { throw it }

    return result as T
}