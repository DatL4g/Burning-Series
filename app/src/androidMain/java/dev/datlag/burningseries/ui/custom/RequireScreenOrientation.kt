package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import dev.datlag.burningseries.common.findActivity

@Composable
fun RequireScreenOrientation(orientation: Int) {
    val viewContext = LocalView.current.context
    val defaultContext = LocalContext.current

    DisposableEffect(Unit) {
        val activity = viewContext?.findActivity() ?: defaultContext.findActivity() ?: return@DisposableEffect onDispose { }
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}