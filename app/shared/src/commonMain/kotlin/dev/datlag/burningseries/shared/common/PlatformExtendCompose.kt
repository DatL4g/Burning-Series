package dev.datlag.burningseries.shared.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimation
import com.arkivanov.essenty.backhandler.BackHandler
import com.vanniktech.blurhash.BlurHash

expect fun Modifier.onClick(
    enabled: Boolean = true,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) : Modifier

expect fun <C : Any, T : Any> backAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit
) : StackAnimation<C, T>

expect fun BlurHash.decode(
    hash: String,
    width: Int,
    height: Int
): ImageBitmap?