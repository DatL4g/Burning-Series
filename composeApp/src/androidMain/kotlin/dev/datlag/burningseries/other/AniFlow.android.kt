package dev.datlag.burningseries.other

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun AniFlow.isInstalled(): Boolean {
    val context = LocalContext.current

    return remember(context) {
        try {
            context.packageManager.getPackageInfo(this.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}