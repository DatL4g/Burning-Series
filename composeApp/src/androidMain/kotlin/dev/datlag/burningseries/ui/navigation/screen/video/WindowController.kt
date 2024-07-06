package dev.datlag.burningseries.ui.navigation.screen.video

import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class WindowController(
    private val view: View,
    private val window: Window?
) {
    private val windowInsetsController = window?.let {
        WindowCompat.getInsetsController(it, view)
    }

    var systemBarsBehavior: Int
        get() = windowInsetsController?.systemBarsBehavior ?: 0
        set(value) {
            windowInsetsController?.systemBarsBehavior = value
        }

    var isStatusBarVisible: Boolean
        get() {
            return ViewCompat.getRootWindowInsets(
                view
            )?.isVisible(WindowInsetsCompat.Type.statusBars()) == true
        }
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.statusBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())
            }
        }

    var isNavigationBarVisible: Boolean
        get() {
            return ViewCompat.getRootWindowInsets(
                view
            )?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true
        }
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }

    var isSystemBarsVisible: Boolean
        get() = isNavigationBarVisible && isStatusBarVisible
        set(value) {
            isStatusBarVisible = value
            isNavigationBarVisible = value
        }

    fun addWindowFlags(flags: Int) {
        window?.addFlags(flags)
    }

    fun clearWindowFlags(flags: Int) {
        window?.clearFlags(flags)
    }
}

@Composable
fun rememberWindowController(
    view: View = LocalView.current,
    window: Window? = (view.parent as? DialogWindowProvider)?.window ?: view.context?.findWindow() ?: LocalContext.current.findWindow()
): WindowController {
    return remember(view, window) { WindowController(view, window) }
}