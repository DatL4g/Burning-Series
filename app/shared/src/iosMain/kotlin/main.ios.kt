import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.uikit.ComposeUIViewControllerDelegate
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.stop
import dev.datlag.burningseries.common.lifecycle.LocalLifecycleOwner
import dev.datlag.burningseries.App
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import org.kodein.di.DI
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val lifecycleRegistry = LifecycleRegistry()

    val lifecycleOwner = object : LifecycleOwner {
        override val lifecycle: Lifecycle = lifecycleRegistry
    }

    val di = DI {
        import(NetworkModule.di)
    }

    val root = NavHostComponent(
        componentContext = DefaultComponentContext(
            lifecycle = lifecycleOwner.lifecycle
        ),
        di = di
    )

    val imageConfig = KamelConfig {
        takeFrom(KamelConfig.Default)
    }

    return ComposeUIViewController(configure = {
        delegate = object : ComposeUIViewControllerDelegate {
            override fun viewDidLoad() {
                super.viewDidLoad()
                lifecycleRegistry.start()
            }

            override fun viewDidAppear(animated: Boolean) {
                super.viewDidAppear(animated)
                lifecycleRegistry.resume()
            }

            override fun viewDidDisappear(animated: Boolean) {
                super.viewDidDisappear(animated)
                lifecycleRegistry.destroy()
            }

            override fun viewWillAppear(animated: Boolean) {
                super.viewWillAppear(animated)
                lifecycleRegistry.resume()
            }

            override fun viewWillDisappear(animated: Boolean) {
                super.viewWillDisappear(animated)
                lifecycleRegistry.stop()
            }
        }
    }) {
        CompositionLocalProvider(
            LocalLifecycleOwner provides lifecycleOwner,
            LocalKamelConfig provides imageConfig
        ) {
            App(di) {
                root.render()
            }
        }
    }
}