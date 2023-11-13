package dev.datlag.burningseries.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Modifier
import androidx.compose.foundation.onClick
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.essenty.backhandler.BackHandler
import com.vanniktech.blurhash.BlurHash
import com.vanniktech.blurhash.BlurHash.decode as defaultDecode

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.onClick(
    enabled: Boolean,
    onDoubleClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onClick: () -> Unit
): Modifier {
    return this.onClick(
        enabled = enabled,
        onDoubleClick = onDoubleClick,
        onLongClick = onLongClick,
        onClick = onClick
    )
}

@OptIn(ExperimentalDecomposeApi::class)
actual fun <C : Any, T : Any> backAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit
): StackAnimation<C, T> = predictiveBackAnimation(
    backHandler = backHandler,
    animation = stackAnimation(fade()),
    onBack = onBack
)

val LocalRestartRequired = compositionLocalOf<Boolean> { false }
actual fun BlurHash.decode(
    hash: String,
    width: Int,
    height: Int
): ImageBitmap? {
    val image = defaultDecode(hash, width, height)
    return image?.toComposeImageBitmap()
}