package dev.datlag.burningseries.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import dev.datlag.burningseries.LocalDI
import dev.datlag.tooling.compose.launchDefault
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.launchMain
import dev.datlag.tooling.decompose.defaultScope
import dev.datlag.tooling.decompose.ioScope
import dev.datlag.tooling.decompose.lifecycle.LocalLifecycleOwner
import dev.datlag.tooling.decompose.mainScope
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.DIAware

interface Component : DIAware, ComponentContext {

    @Composable
    fun render()

    fun launchIO(block: suspend CoroutineScope.() -> Unit) = ioScope().launchIO(block)
    fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope().launchMain(block)
    fun launchDefault(block: suspend CoroutineScope.() -> Unit) = defaultScope().launchDefault(block)

    @Composable
    fun Component.onRender(content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalDI provides di,
            LocalLifecycleOwner provides object : LifecycleOwner {
                override val lifecycle: Lifecycle = this@onRender.lifecycle
            }
        ) {
            content()
        }
        SideEffect {
            // nullableFirebaseInstance()?.crashlytics?.screen(this)
        }
    }
}