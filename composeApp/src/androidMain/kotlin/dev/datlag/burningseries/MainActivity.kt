package dev.datlag.burningseries

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.backHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import dev.datlag.burningseries.other.DownloadManager
import dev.datlag.burningseries.ui.navigation.RootComponent
import dev.datlag.kast.Kast
import dev.datlag.kast.UnselectReason
import dev.datlag.tooling.decompose.lifecycle.LocalLifecycleOwner
import dev.datlag.tooling.safeCast
import io.ktor.client.HttpClient
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val di = applicationContext.safeCast<DIAware>()?.di ?: (application as DIAware).di
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle = essentyLifecycle()
        }
        val authFactory by di.instance<AndroidCodeAuthFlowFactory>()
        val httpClient by di.instance<HttpClient>()
        val appContext by di.instanceOrNull<Context>()

        val root = RootComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycleOwner.lifecycle,
                backHandler = backHandler()
            ),
            di = di
        )

        authFactory.registerActivity(this)
        Kast.setup(this)
        DownloadManager.setClient(httpClient).setFile(appContext ?: this)

        setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner,
                LocalEdgeToEdge provides true
            ) {
                App(
                    di = di
                ) {
                    root.render()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Kast.unselect(UnselectReason.disconnected)
        Kast.dispose()
    }
}