package dev.datlag.burningseries

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.PredictiveBackGestureIcon
import com.arkivanov.decompose.extensions.compose.jetbrains.PredictiveBackGestureOverlay
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.google.firebase.FirebasePlatform
import dev.datlag.burningseries.common.lifecycle.LocalLifecycleOwner
import dev.datlag.burningseries.model.common.systemProperty
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import dev.datlag.burningseries.window.disposableSingleWindowApplication
import dev.datlag.sekret.NativeLoader
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.config.resourcesFetcher
import io.kamel.image.config.svgDecoder
import org.kodein.di.DI
import java.io.File

fun main(vararg args: String) {
    runWindow()
}

@OptIn(ExperimentalDecomposeApi::class)
private fun runWindow() {
    val appTitle = StringDesc.Resource(SharedRes.strings.app_name).localized()
    AppIO.applyTitle(appTitle)
    Napier.base(DebugAntilog())

    StateSaver.sekretLibraryLoaded = NativeLoader.loadLibrary("sekret", systemProperty("compose.application.resources.dir")?.let { File(it) })

    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
        val storage = mutableMapOf<String, String>()
        override fun store(key: String, value: String) = storage.set(key, value)
        override fun retrieve(key: String) = storage[key]
        override fun clear(key: String) { storage.remove(key) }
        override fun log(msg: String) = println(msg)
    })

    val windowState = WindowState()
    val lifecycle = LifecycleRegistry()
    val lifecycleOwner = object : LifecycleOwner {
        override val lifecycle: Lifecycle = lifecycle
    }
    val backDispatcher = BackDispatcher()
    val di = DI {
        import(NetworkModule.di)
    }
    val root = NavHostComponent(
        componentContext = DefaultComponentContext(
            lifecycle,
            backHandler = backDispatcher
        ),
        di = di
    )
    val imageConfig = KamelConfig {
        takeFrom(KamelConfig.Default)
        resourcesFetcher()
    }

    disposableSingleWindowApplication(
        state = windowState,
        title = appTitle,
        onKeyEvent = {
            false
        },
        exitProcessOnExit = true
    ) {
        LifecycleController(lifecycle, windowState)

        InitCEF {
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner,
                LocalWindow provides this.window,
                LocalKamelConfig provides imageConfig
            ) {
                App(di) {
                    PredictiveBackGestureOverlay(
                        backDispatcher = backDispatcher,
                        backIcon = { progress, _ ->
                            PredictiveBackGestureIcon(
                                imageVector = Icons.Default.ArrowBackIosNew,
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
}