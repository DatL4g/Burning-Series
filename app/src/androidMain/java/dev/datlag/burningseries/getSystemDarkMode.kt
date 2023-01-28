package dev.datlag.burningseries

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
actual fun getSystemDarkMode(): Boolean {
    return false
}

@Composable
actual fun setSystemAppearance(themeMode: Int, usesDarkMode: Boolean, usesAmoled: Boolean) {
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current

    if (usesDarkMode && usesAmoled) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        systemUiController.setStatusBarColor(Color(context.getColor(R.color.statusBarColorAmoled)))
    } else {
        val mode = when (themeMode) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        systemUiController.setStatusBarColor(Color(context.getColor(R.color.statusBarColor)))
    }
}