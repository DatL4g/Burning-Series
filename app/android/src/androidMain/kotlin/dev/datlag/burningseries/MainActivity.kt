package dev.datlag.burningseries

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.backHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.google.android.gms.cast.framework.CastContext
import dev.datlag.burningseries.common.lifecycle.LocalLifecycleOwner
import dev.datlag.burningseries.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.ui.KeyEventDispatcher
import dev.datlag.burningseries.ui.PIPActions
import dev.datlag.burningseries.ui.PIPEventDispatcher
import dev.datlag.burningseries.ui.PIPModeListener
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import dev.datlag.burningseries.ui.screen.video.LocalCastContext
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.config.resourcesFetcher
import io.kamel.image.config.resourcesIdMapper
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var castContext: MutableStateFlow<CastContext?> = MutableStateFlow(null)

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
                lifecycle = lifecycleOwner.lifecycle,
                backHandler = backHandler()
            ),
            di = di
        )

        CastContext.getSharedInstance(this, Executors.newSingleThreadExecutor()).addOnCompleteListener {
            val result = it.result ?: CastContext.getSharedInstance()
            castContext.value = result
        }

        setContent {
            CompositionLocalProvider(
                LocalKamelConfig provides imageConfig,
                LocalLifecycleOwner provides lifecycleOwner
            ) {
                val cast by castContext.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalCastContext provides cast
                ) {
                    App(di) {
                        root.render()
                    }
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return (KeyEventDispatcher.invoke(event) ?: false) || super.dispatchKeyEvent(event)
    }

    override fun onUserLeaveHint() {
        if (PIPEventDispatcher.invoke() == true) {
            enterPIPMode()
        } else {
            super.onUserLeaveHint()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        PIPModeListener.invoke(isInPictureInPictureMode)
    }

    private fun enterPIPMode() {
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true)
        }

        val actions = PIPActions.invoke()
        if (!actions.isNullOrEmpty()) {
            builder.setActions(actions)
        }

        this.enterPictureInPictureMode(builder.build())
    }
}