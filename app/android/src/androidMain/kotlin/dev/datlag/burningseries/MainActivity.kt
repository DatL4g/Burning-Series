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
import dev.datlag.burningseries.shared.App
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.lifecycle.LocalLifecycleOwner
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

        val di = ((applicationContext as? App) ?: (application as App)).di

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

        Kast.setup(this)
        SmallIcon = R.drawable.ic_launcher_foreground

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