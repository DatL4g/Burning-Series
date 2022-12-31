package dev.datlag.burningseries.common

import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat

fun Window?.enterFullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
    this?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    this?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
}

fun Window?.exitFullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
    }
    this?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    this?.let { WindowCompat.setDecorFitsSystemWindows(it, true) }
}