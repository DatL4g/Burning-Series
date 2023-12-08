package dev.datlag.burningseries.shared.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import dev.datlag.burningseries.shared.ui.theme.shape.DiagonalShape
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

fun Modifier.isFocused(
    hoverable: Boolean = true,
    focusable: Boolean = true,
    builder: Modifier.() -> Modifier
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }

    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    this.ifTrue(isHovered || isFocused) {
        builder()
    }.hoverable(
        interactionSource = interactionSource,
        enabled = hoverable
    ).focusable(
        interactionSource = interactionSource,
        enabled = focusable
    )
}

fun Modifier.focusScale(
    scale: Float = 1.1F,
    hoverable: Boolean = true,
    focusable: Boolean = true
) = composed {
    isFocused(
        hoverable,
        focusable
    ) {
        graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }
}

@Composable
fun ColorScheme.animate(spec: AnimationSpec<Color> = spring(stiffness = Spring.StiffnessLow)): ColorScheme {
    return this.copy(
        primary = animateColorAsState(this.primary, spec).value,
        onPrimary = animateColorAsState(this.onPrimary, spec).value,
        primaryContainer = animateColorAsState(this.primaryContainer, spec).value,
        onPrimaryContainer = animateColorAsState(this.onPrimaryContainer, spec).value,
        secondary = animateColorAsState(this.secondary, spec).value,
        onSecondary = animateColorAsState(this.onSecondary, spec).value,
        secondaryContainer = animateColorAsState(this.secondaryContainer, spec).value,
        onSecondaryContainer = animateColorAsState(this.onSecondaryContainer, spec).value,
        tertiary = animateColorAsState(this.tertiary, spec).value,
        onTertiary = animateColorAsState(this.onTertiary, spec).value,
        tertiaryContainer = animateColorAsState(this.tertiaryContainer, spec).value,
        onTertiaryContainer = animateColorAsState(this.onTertiaryContainer, spec).value,
        background = animateColorAsState(this.background, spec).value,
        onBackground = animateColorAsState(this.onBackground, spec).value,
        surface = animateColorAsState(this.surface, spec).value,
        onSurface = animateColorAsState(this.onSurface, spec).value,
        error = animateColorAsState(this.error, spec).value,
        onError = animateColorAsState(this.onError, spec).value,
        errorContainer = animateColorAsState(this.errorContainer, spec).value,
        onErrorContainer = animateColorAsState(this.onErrorContainer, spec).value
    )
}