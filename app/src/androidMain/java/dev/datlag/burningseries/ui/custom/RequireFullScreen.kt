package dev.datlag.burningseries.ui.custom

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.datlag.burningseries.common.enterFullScreen
import dev.datlag.burningseries.common.exitFullScreen
import dev.datlag.burningseries.common.findWindow

@Composable
fun RequireFullScreen() {
    val window = LocalView.current.context.findWindow() ?: LocalContext.current.findWindow()
    val systemUiController = rememberSystemUiController()

    DisposableEffect(Unit) {
        val originalBehavior = systemUiController.systemBarsBehavior

        systemUiController.isSystemBarsVisible = false
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.enterFullScreen()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON and WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            systemUiController.isSystemBarsVisible = true
            systemUiController.systemBarsBehavior = originalBehavior
            window.exitFullScreen()
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON and WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}