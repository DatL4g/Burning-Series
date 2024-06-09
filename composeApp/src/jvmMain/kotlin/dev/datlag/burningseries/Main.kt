package dev.datlag.burningseries

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
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
// import dev.datlag.burningseries.firebase.FirebaseFactory
// import dev.datlag.burningseries.firebase.initializePlatform
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.other.StateSaver
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

val LocalWindow = staticCompositionLocalOf<ComposeWindow> { error("No Window provided") }

fun main(vararg args: String) {
    Napier.base(DebugAntilog())
    StateSaver.sekretLibraryLoaded = NativeLoader.loadLibrary(
        name = "sekret",
        path = systemProperty("compose.application.resources.dir")?.let(::File)
    )
    FirebaseFactory.initializePlatform()
    Kast.restartDiscovery()

    val di = DI {
        systemProperty("jpackage.app-version")?.let {
            bindSingleton("APP_VERSION") { it }
        }

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