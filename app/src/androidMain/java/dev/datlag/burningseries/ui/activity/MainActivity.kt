package dev.datlag.burningseries.ui.activity

import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.KeyEvent
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistryOwner
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.backHandler
import com.arkivanov.essenty.instancekeeper.instanceKeeper
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.google.android.gms.cast.framework.CastContext
import dev.datlag.burningseries.*
import dev.datlag.burningseries.common.collectAsStateSafe
import dev.datlag.burningseries.common.getSafeParcelable
import dev.datlag.burningseries.common.getSizeInBytes
import dev.datlag.burningseries.common.safeEmit
import dev.datlag.burningseries.helper.NightMode
import dev.datlag.burningseries.other.*
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import dev.datlag.burningseries.ui.screen.video.LocalCastContext
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val castContext: MutableStateFlow<CastContext?> = MutableStateFlow(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            this.setTheme(R.style.AppTheme)
        } else {
            installSplashScreen()
        }
        super.onCreate(savedInstanceState)

        val di = ((applicationContext as? App) ?: (application as App)).di
        val nightMode = NightMode.Helper(this).getMode().value

        val root = NavHostComponent.create(
            componentContext = DefaultComponentContext(
                lifecycle = essentyLifecycle(),
                stateKeeper = stateKeeper(onBundleTooLarge = {
                    StateSaver.state[KEY_STATE] = it
                }),
                instanceKeeper = instanceKeeper(),
                backHandler = backHandler()
            ),
            di
        )
        val resources = Resources(assets)
        val stringRes = StringRes(this)

        CastContext.getSharedInstance(this, Executors.newSingleThreadExecutor())
            .addOnCompleteListener {
                val result = it.result ?: CastContext.getSharedInstance()
                castContext.safeEmit(result, lifecycleScope)
            }

        setContent {
            val configuration = LocalConfiguration.current
            val orientation = when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
                else -> Orientation.PORTRAIT
            }

            val cast by castContext.collectAsStateSafe()

            CompositionLocalProvider(
                LocalResources provides resources,
                LocalStringRes provides stringRes,
                LocalOrientation provides orientation,
                LocalCastContext provides cast
            ) {
                App(di, nightMode) {
                    root.render()
                }
            }
        }

        NavigationListener = { finish ->
            if (finish) {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            BackPressedListener?.invoke()
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

    @Suppress("DEPRECATION")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)

        PIPModeListener.invoke(isInPictureInPictureMode)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        PIPModeListener.invoke(isInPictureInPictureMode)
    }

    @Suppress("DEPRECATION")
    private fun enterPIPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.enterPictureInPictureMode()
        }
    }

    private fun SavedStateRegistryOwner.stateKeeper(onBundleTooLarge: (ParcelableContainer) -> Unit = {}): StateKeeper {
        val dispatcher = StateKeeperDispatcher(
            savedStateRegistry.consumeRestoredStateForKey(KEY_STATE)?.getSafeParcelable(KEY_STATE) ?: StateSaver.state[KEY_STATE]
        )

        savedStateRegistry.registerSavedStateProvider(KEY_STATE) {
            val savedState = dispatcher.save()
            val bundle = Bundle()

            if (savedState.getSizeInBytes() <= SAVED_STATE_MAX_SIZE) {
                bundle.putParcelable(KEY_STATE, savedState)
            } else {
                onBundleTooLarge(savedState)
            }

            bundle
        }

        return dispatcher
    }

    companion object {
        private const val KEY_STATE = "STATE_KEEPER_STATE"
        private const val SAVED_STATE_MAX_SIZE = 500_000
    }
}

var KeyEventDispatcher: (event: KeyEvent?) -> Boolean? = { null }
var PIPEventDispatcher: () -> Boolean? = { null }
var PIPModeListener: (Boolean) -> Unit = { }
var PIPActions: () -> ArrayList<RemoteAction>? = { null }