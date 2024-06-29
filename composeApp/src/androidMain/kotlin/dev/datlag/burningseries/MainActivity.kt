package dev.datlag.burningseries

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.backHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.other.Constants
import dev.datlag.burningseries.other.DomainVerifier
import dev.datlag.burningseries.other.DownloadManager
import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.ui.navigation.RootComponent
import dev.datlag.kast.Kast
import dev.datlag.kast.UnselectReason
import dev.datlag.tooling.decompose.lifecycle.LocalLifecycleOwner
import dev.datlag.tooling.safeCast
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory

class MainActivity : ComponentActivity() {

    private lateinit var root: RootComponent

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

        root = RootComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycleOwner.lifecycle,
                backHandler = backHandler()
            ),
            di = di,
            syncId = intent.data?.findSyncId(),
            seriesHref = intent.data?.findSeries()
        )

        authFactory.registerActivity(this)
        Kast.setup(this)
        DownloadManager.setClient(httpClient).setFile(appContext ?: this)
        DomainVerifier.verify(this)

        setContent {
            val appSettings by di.instance<Settings.PlatformAppSettings>()
            LaunchedEffect(Unit) {
                appSettings.increaseStartCounter()
            }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent.data?.findSyncId()?.let(root::onSync)
            ?: intent.data?.findSeries()?.let(root::onSeries)
    }

    override fun onDestroy() {
        super.onDestroy()

        Kast.unselect(UnselectReason.disconnected)
        Kast.dispose()
        DomainVerifier.verify(this)
    }

    override fun onStart() {
        super.onStart()

        DomainVerifier.verify(this)
    }

    override fun onResume() {
        super.onResume()

        DomainVerifier.verify(this)
    }

    override fun onPause() {
        super.onPause()

        DomainVerifier.verify(this)
    }

    override fun onRestart() {
        super.onRestart()

        DomainVerifier.verify(this)
    }

    private fun Uri.findSyncId(): String? {
        val matchingHost = this.host.equals(Constants.SYNCING_DOMAIN, ignoreCase = true)
        if (matchingHost) {
            val matchingPath = this.pathSegments.firstOrNull()?.equals("sync", ignoreCase = true) == true

            if (matchingPath) {
                val id = lastPathSegment?.ifBlank {
                    null
                } ?: pathSegments.lastOrNull()?.ifBlank {
                    null
                }

                return id
            }
        }

        return null
    }

    private fun Uri.findSeries(): String? {
        return BSUtil.matchingUrl(this.toString(), this.path)?.let(BSUtil::fixSeriesHref)?.ifBlank { null }
    }
}