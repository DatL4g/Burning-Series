package dev.datlag.burningseries

import androidx.compose.runtime.Composable
import dev.datlag.burningseries.other.Constants
import evalBash
import org.apache.commons.lang3.SystemUtils

@Composable
actual fun getSystemDarkMode(): Boolean {
    return if (SystemUtils.IS_OS_LINUX) {
        (Constants.LINUX_DARK_MODE_CMD.evalBash(env = null).getOrDefault(String())).ifEmpty {
            Constants.LINUX_DARK_MODE_LEGACY_CMD.evalBash(env = null).getOrDefault(String())
        }.contains("dark", true)
    } else {
        false
    }
}

@Composable
actual fun setSystemAppearance(themeMode: Int, usesDarkMode: Boolean, usesAmoled: Boolean) { }