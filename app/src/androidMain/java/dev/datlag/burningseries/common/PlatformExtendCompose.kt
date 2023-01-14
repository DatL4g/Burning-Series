package dev.datlag.burningseries.common

import android.content.res.ColorStateList
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.google.android.material.shape.ShapeAppearanceModel

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.onClick(
    enabled: Boolean,
    onDoubleClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onClick: () -> Unit
): Modifier {
    return this.combinedClickable(
        enabled = enabled,
        onDoubleClick = onDoubleClick,
        onLongClick = onLongClick,
        onClick = onClick
    )
}

@Composable
fun CornerBasedShape.toLegacyShape(): ShapeAppearanceModel {
    val density = LocalDensity.current
    val size = Size.Unspecified

    return ShapeAppearanceModel.Builder()
        .setTopLeftCornerSize(this.topStart.toPx(size, density))
        .setTopRightCornerSize(this.topEnd.toPx(size, density))
        .setBottomLeftCornerSize(this.bottomStart.toPx(size, density))
        .setBottomRightCornerSize(this.bottomEnd.toPx(size, density))
        .build()
}

@Composable
fun ButtonDefaults.legacyButtonTintList(
    containerColor: Color
): ColorStateList {
    val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    val colors = intArrayOf(containerColor.toArgb())
    return ColorStateList(states, colors)
}

@Composable
actual fun isTv(): Boolean {
    val context = LocalContext.current
    return remember { (context.packageManager ?: context.applicationContext.packageManager).isTelevision() }
}