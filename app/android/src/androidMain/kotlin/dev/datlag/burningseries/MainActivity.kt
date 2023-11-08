package dev.datlag.burningseries

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import dev.datlag.burningseries.common.lifecycle.LocalLifecycleOwner
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.config.resourcesFetcher
import io.kamel.image.config.resourcesIdMapper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val di = ((applicationContext as? App) ?: (application as App)).di
        val imageConfig = KamelConfig {
            takeFrom(KamelConfig.Default)
            resourcesFetcher(this@MainActivity)
            resourcesIdMapper(this@MainActivity)
        }

        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle = essentyLifecycle()
        }

        val root = NavHostComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycleOwner.lifecycle
            ),
            di = di
        )

        setContent {
            CompositionLocalProvider(
                LocalKamelConfig provides imageConfig,
                LocalLifecycleOwner provides lifecycleOwner
            ) {
                App(di) {
                    root.render()
                }
            }
        }
    }
}