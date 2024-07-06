package dev.datlag.burningseries.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import dev.datlag.burningseries.LocalDI
import dev.datlag.burningseries.PictureInPicture
import dev.datlag.burningseries.common.nullableFirebaseInstance
import dev.datlag.burningseries.ui.custom.PIPContent
import dev.datlag.burningseries.ui.theme.SchemeTheme
import dev.datlag.tooling.compose.launchDefault
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.launchMain
import dev.datlag.tooling.decompose.defaultScope
import dev.datlag.tooling.decompose.ioScope
import dev.datlag.tooling.decompose.lifecycle.LocalLifecycleOwner
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import dev.datlag.tooling.decompose.mainScope
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.DIAware

interface Component : DIAware, ComponentContext {

    val handlesPIP: Boolean
        get() = false

    @Composable
    fun render()

    fun launchIO(block: suspend CoroutineScope.() -> Unit) = ioScope().launchIO(block)
    fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope().launchMain(block)
    fun launchDefault(block: suspend CoroutineScope.() -> Unit) = defaultScope().launchDefault(block)

    @Composable
    fun onRender(content: @Composable (Boolean) -> Unit) {
        CompositionLocalProvider(
            LocalDI provides di,
            LocalLifecycleOwner provides object : LifecycleOwner {
                override val lifecycle: Lifecycle = this@Component.lifecycle
            }
        ) {
            Box {
                val pip by PictureInPicture.collectAsStateWithLifecycle()

                content(pip)
                if (pip && !handlesPIP) {
                    PIPContent()
                }
            }
        }
        SideEffect {
            // nullableFirebaseInstance()?.crashlytics?.screen(this)
        }
    }

    @Composable
    fun onRenderWithScheme(key: Any?, content: @Composable (SchemeTheme.Updater?) -> Unit) {
        onRender {
            SchemeTheme(key = key, content = content)
        }
    }
}