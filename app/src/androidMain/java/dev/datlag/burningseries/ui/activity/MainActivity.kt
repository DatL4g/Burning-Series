package dev.datlag.burningseries.ui.activity

import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Rational
import android.view.KeyEvent
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.os.BuildCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.savedstate.SavedStateRegistryOwner
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.backhandler.backHandler
import com.arkivanov.essenty.instancekeeper.instanceKeeper
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import dev.datlag.burningseries.*
import dev.datlag.burningseries.common.getSafeParcelable
import dev.datlag.burningseries.common.getSizeInBytes
import dev.datlag.burningseries.datastore.preferences.AppSettings
import dev.datlag.burningseries.helper.NightMode
import dev.datlag.burningseries.module.DataStoreModule
import dev.datlag.burningseries.module.NetworkModule
import dev.datlag.burningseries.module.PlatformModule
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.other.Resources
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.other.StringRes
import dev.datlag.burningseries.ui.navigation.NavHostComponent
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

class MainActivity : AppCompatActivity() {

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

        setContent {
            val configuration = LocalConfiguration.current
            val orientation = when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
                else -> Orientation.PORTRAIT
            }

            CompositionLocalProvider(
                LocalResources provides resources,
                LocalStringRes provides stringRes,
                LocalOrientation provides orientation
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