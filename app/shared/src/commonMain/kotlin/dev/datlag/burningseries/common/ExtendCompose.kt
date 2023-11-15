package dev.datlag.burningseries.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import dev.datlag.burningseries.ui.theme.shape.DiagonalShape
import kotlin.math.max

inline fun Modifier.ifTrue(predicate: Boolean, builder: Modifier.() -> Modifier) = then(if (predicate) builder() else Modifier)
inline fun Modifier.ifFalse(predicate: Boolean, builder: Modifier.() -> Modifier) = then(if (!predicate) builder() else Modifier)

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

fun Modifier.bounceClick(minScale: Float = 0.9F) = composed {
    var buttonState by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (buttonState) minScale else 1F)

    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.pointerInput(buttonState) {
        awaitPointerEventScope {
            buttonState = if (buttonState) {
                waitForUpOrCancellation()
                false
            } else {
                awaitFirstDown(false)
                true
            }
        }
    }
}

fun Modifier.pressClick(maxTranslation: Float = 10F) = composed {
    var buttonState by remember { mutableStateOf(false) }
    val translation by animateFloatAsState(if (buttonState) maxTranslation else 0F)

    graphicsLayer {
        translationY = translation
    }.pointerInput(buttonState) {
        awaitPointerEventScope {
            buttonState = if (buttonState) {
                waitForUpOrCancellation()
                false
            } else {
                awaitFirstDown(false)
                true
            }
        }
    }
}

fun Modifier.diagonalShape(
    angle: Float,
    position: DiagonalShape.POSITION = DiagonalShape.POSITION.TOP
) = this.clip(DiagonalShape(angle, position))

fun DpSize.toSize(): Size {
    return Size(
        width = this.width.value,
        height = this.height.value
    )
}

@Composable
fun LazyListState.OnBottomReached(enabled: Boolean = true, buffer: Int = 0, block: () -> Unit) {
    if (enabled) {
        val maxBuffer = max(0, buffer)

        val shouldCallBlock = remember {
            derivedStateOf {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true
                lastVisibleItem.index == layoutInfo.totalItemsCount - 1 - maxBuffer
            }
        }

        LaunchedEffect(shouldCallBlock) {
            snapshotFlow { shouldCallBlock.value }.collect {
                if (it) {
                    block()
                }
            }
        }
    }
}