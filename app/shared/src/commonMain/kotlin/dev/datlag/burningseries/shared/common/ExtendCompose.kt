package dev.datlag.burningseries.shared.common

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathBuilder
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

@Composable
fun Modifier.isFocused(
    hoverable: Boolean = true,
    focusable: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    builder: Modifier.() -> Modifier
): Modifier = composed {
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

@Composable
fun Modifier.onFocusChanged(
    hoverable: Boolean = true,
    focusable: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onChanged: (Boolean) -> Unit
): Modifier = composed {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val latestValue = remember(isHovered, isFocused) { isHovered || isFocused }

    LaunchedEffect(latestValue) {
        onChanged(latestValue)
    }

    this.hoverable(
        interactionSource = interactionSource,
        enabled = hoverable
    ).focusable(
        interactionSource = interactionSource,
        enabled = focusable
    )
}

@Composable
fun Modifier.focusScale(
    scale: Float = 1.1F,
    hoverable: Boolean = true,
    focusable: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = composed {
    isFocused(
        hoverable,
        focusable,
        interactionSource
    ) {
        graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }
}

fun PathBuilder.drawPathFromSvgData(data: String) {
    val pathCommands = data.split("(?=[a-zA-Z])".toRegex()).filterNot { it.isBlank() }
    val numberRegex = "[-+]?\\d*\\.?\\d+".toRegex()

    for (command in pathCommands) {
        val cmd = command.substring(0, 1).trim()
        val coords = numberRegex.findAll(command.substring(1)).mapNotNull { it.value.toFloatOrNull() }.toList()
        println(coords)

        when (cmd) {
            "M" -> moveTo(coords[0], coords[1])
            "m" -> moveToRelative(coords[0], coords[1])
            "L" -> lineTo(coords[0], coords[1])
            "l" -> lineToRelative(coords[0], coords[1])
            "Q" -> quadTo(coords[0], coords[1], coords[2], coords[3])
            "q" -> quadToRelative(coords[0], coords[1], coords[2], coords[3])
            "T" -> reflectiveQuadTo(coords[0], coords[1])
            "t" -> reflectiveQuadToRelative(coords[0], coords[1])
            "C" -> curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])
            "c" -> curveToRelative(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])
            "S" -> reflectiveCurveTo(coords[0], coords[1], coords[2], coords[3])
            "s" -> reflectiveCurveToRelative(coords[0], coords[1], coords[2], coords[3])
            "V" -> verticalLineTo(coords[0])
            "v" -> verticalLineToRelative(coords[0])
            "H" -> horizontalLineTo(coords[0])
            "h" -> horizontalLineToRelative(coords[0])
            "Z", "z" -> close()
        }
    }
}