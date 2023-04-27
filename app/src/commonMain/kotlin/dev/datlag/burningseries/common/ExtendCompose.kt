package dev.datlag.burningseries.common

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.datlag.burningseries.LocalOrientation
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.ui.custom.dragdrop.DragDropState
import kotlinx.coroutines.flow.Flow

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

val Color.Companion.SemiBlack: Color
    get() = Color.Black.copy(alpha = 0.5F)

val Color.Companion.Success: Color
    get() = Color(58, 206, 162)

val Color.Companion.Warning: Color
    get() = Color(245, 205, 103)

val Color.Companion.OnWarning: Color
    get() = Color(18, 17, 14)

@Composable
fun ImageVector.painter(tint: Color = this.tintColor, blendMode: BlendMode = this.tintBlendMode): Painter {
    return rememberVectorPainter(
        this.defaultWidth,
        this.defaultHeight,
        this.viewportWidth,
        this.viewportHeight,
        this.name,
        tint,
        blendMode,
        this.autoMirror
    ) { _, _ ->
        RenderVectorGroup(this.root)
    }
}

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onSwap: (Int, Int) -> Unit
): DragDropState {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState(
            state = lazyListState,
            onSwap = onSwap,
            scope = scope
        )
    }
    return state
}

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this
        .layoutInfo
        .visibleItemsInfo
        .getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)
}

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem(
    dragDropState: DragDropState,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(isDragging: Boolean) -> Unit
) {
    val current: Float by animateFloatAsState(dragDropState.draggingItemOffset * 0.67f)
    val previous: Float by animateFloatAsState(dragDropState.previousItemOffset.value * 0.67f)
    val dragging = index == dragDropState.currentIndexOfDraggedItem
    val draggingModifier = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = current
            }
    } else if (index == dragDropState.previousIndexOfDraggedItem) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = previous
            }
    } else {
        Modifier.animateItemPlacement(
            tween(easing = FastOutLinearInEasing)
        )
    }
    Column(modifier = modifier.then(draggingModifier)) {
        content(dragging)
    }
}

@Composable
fun Modifier.fillWidthInPortraitMode(fraction: Float = 1f): Modifier {
    return if (LocalOrientation.current is Orientation.PORTRAIT) {
        this.fillMaxWidth(fraction)
    } else {
        this
    }
}

@Composable
fun Modifier.cardItemSize(): Modifier {
    return if (isTv()) {
        this.width(200.dp)
    } else {
        this.fillMaxWidth()
    }
}

@Composable
fun Modifier.focusRequesterIf(requester: FocusRequester?, predicate: Boolean): Modifier {
    return if(requester != null && predicate) {
        this.focusRequester(requester)
    } else {
        this
    }
}

@Composable
fun Modifier.focusRequesterIf(requester: FocusRequester?, predicate: () -> Boolean): Modifier {
    return if(requester != null && predicate()) {
        this.focusRequester(requester)
    } else {
        this
    }
}

@Composable
fun RunOnce(key: Any?, orCheck: () -> Boolean = { false }, block: () -> Unit) {
    SideEffect {
        if (!StateSaver.runOnce.getOrDefault(key, false) || orCheck()) {
            block()
            StateSaver.runOnce[key] = true
        }
    }
    DisposableEffect(key) {
        onDispose {
            StateSaver.runOnce.remove(key)
        }
    }
}

inline fun Modifier.ifTrue(predicate: Boolean, builder: Modifier.() -> Modifier) = then(if (predicate) builder() else Modifier)
inline fun Modifier.ifFalse(predicate: Boolean, builder: Modifier.() -> Modifier) = then(if (!predicate) builder() else Modifier)

inline fun <T : Any> Modifier.ifNotNull(value: T?, builder: (T) -> Modifier): Modifier = then(if (value != null) builder(value) else Modifier)

@Composable
fun Modifier.ifFocused(
    hoverable: Boolean = true,
    focusable: Boolean = true,
    builder: Modifier.() -> Modifier
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }

    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    return this.ifTrue(isHovered || isFocused) {
        builder(this)
    }.hoverable(
        interactionSource = interactionSource,
        enabled = hoverable
    ).focusable(
        interactionSource = interactionSource,
        enabled = focusable
    )
}

@Composable
fun Modifier.focusBorder(
    color: Color,
    space: Dp = 2.dp,
    shape: Shape = CircleShape,
    hoverable: Boolean = true,
    focusable: Boolean = true
): Modifier {
    return this.ifFocused(
        hoverable = hoverable,
        focusable = focusable
    ) {
        padding(space)
        border(2.dp, color, shape)
    }
}
