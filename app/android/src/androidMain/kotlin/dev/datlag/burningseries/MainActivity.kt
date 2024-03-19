package dev.datlag.burningseries

import android.Manifest
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.backHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.shouldShowRationale
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.common.safeCast
import dev.datlag.burningseries.shared.App
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.lifecycle.LocalLifecycleOwner
import dev.datlag.burningseries.shared.other.DomainVerifier
import dev.datlag.burningseries.shared.ui.*
import dev.datlag.burningseries.shared.ui.custom.Permission
import dev.datlag.burningseries.shared.ui.navigation.NavHostComponent
import dev.datlag.kast.Kast
import dev.icerock.moko.resources.compose.stringResource

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            this.setTheme(R.style.AppTheme)
        } else {
            installSplashScreen()
        }
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val di = applicationContext.safeCast<App>()?.di ?: (application as App).di

        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle = essentyLifecycle()
        }

        val shortcutIntent = BSUtil.getIntentDataUrl(intent?.data?.toString())
        val root = NavHostComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycleOwner.lifecycle,
                backHandler = backHandler()
            ),
            di = di,
            shortcutIntent = shortcutIntent
        )

        Kast.setup(this)
        SmallIcon = R.drawable.ic_launcher_foreground
        DomainVerifier.verify(this)

        setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner
            ) {
                App(di) {
                    root.render()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        var showDialog by remember { mutableStateOf(true) }
                        var showDialogAgain by remember { mutableStateOf(true) }

                        Permission(
                            permission = Manifest.permission.POST_NOTIFICATIONS,
                            onGranted = {
                                NotificationPermission = true
                            },
                            onShowInfo = {
                                if (showDialog && showDialogAgain) {
                                    val text = if (it.status.shouldShowRationale) {
                                        SharedRes.strings.permission_notification_rational
                                    } else {
                                        SharedRes.strings.permission_notification
                                    }

                                    NotificationDialog(
                                        text = stringResource(text),
                                        onConfirm = {
                                            it.launchPermissionRequest()
                                        },
                                        onDismiss = { force ->
                                            showDialog = false
                                            showDialogAgain = !force
                                        }
                                    )
                                }
                            },
                            onDeniedForever = {
                                showDialog = false
                                showDialogAgain = false
                            }
                        )
                    } else {
                        NotificationPermission = true
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

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

    override fun onStop() {
        super.onStop()

        DomainVerifier.verify(this)
    }

    override fun onRestart() {
        super.onRestart()

        DomainVerifier.verify(this)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return (KeyEventDispatcher.invoke(event) ?: false) || super.dispatchKeyEvent(event)
    }

    override fun onUserLeaveHint() {
        if (PIPEnabled) {
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
        if (!PIPEnabled) {
            return
        }

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

    override fun enterPictureInPictureMode(params: PictureInPictureParams): Boolean {
        return if (!PIPEnabled) {
            false
        } else {
            super.enterPictureInPictureMode(params)
        }
    }
}