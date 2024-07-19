package dev.datlag.burningseries.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.max
import com.kmpalette.DominantColorState
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.colorScheme

@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current

    return PaddingValues(
        start = this.calculateStartPadding(direction) + other.calculateStartPadding(direction),
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        end = this.calculateEndPadding(direction) + other.calculateEndPadding(direction),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding()
    )
}

@Composable
operator fun PaddingValues.plus(all: Dp): PaddingValues {
    val direction = LocalLayoutDirection.current
    val other = PaddingValues(all)

    return PaddingValues(
        start = this.calculateStartPadding(direction) + other.calculateStartPadding(direction),
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        end = this.calculateEndPadding(direction) + other.calculateEndPadding(direction),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding()
    )
}

@Composable
fun PaddingValues.merge(other: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current

    return PaddingValues(
        start = max(this.calculateStartPadding(direction), other.calculateStartPadding(direction)),
        top = max(this.calculateTopPadding(), other.calculateTopPadding()),
        end = max(this.calculateEndPadding(direction), other.calculateEndPadding(direction)),
        bottom = max(this.calculateBottomPadding(), other.calculateBottomPadding())
    )
}

@Composable
fun PaddingValues.merge(all: Dp): PaddingValues {
    val direction = LocalLayoutDirection.current
    val other = PaddingValues(all)

    return PaddingValues(
        start = max(this.calculateStartPadding(direction), other.calculateStartPadding(direction)),
        top = max(this.calculateTopPadding(), other.calculateTopPadding()),
        end = max(this.calculateEndPadding(direction), other.calculateEndPadding(direction)),
        bottom = max(this.calculateBottomPadding(), other.calculateBottomPadding())
    )
}

val <T : Any> DominantColorState<T>?.primary
    @Composable
    get() = this?.color ?: Platform.colorScheme().primary

val <T : Any> DominantColorState<T>?.onPrimary
    @Composable
    get() = this?.onColor ?: Platform.colorScheme().onPrimary

val Color.plainOnColor: Color
    get() = if (this.luminance() > 0.5F) {
        Color.Black
    } else {
        Color.White
    }

fun Modifier.bottomShadowBrush(color: Color, alpha: Float = 1F): Modifier {
    val maxAlpha = kotlin.math.min(alpha, 1F)

    return this.background(
        brush = Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.1f to color.copy(alpha = 0.35f * maxAlpha),
            0.3f to color.copy(alpha = 0.55f * maxAlpha),
            0.5f to color.copy(alpha = 0.75f * maxAlpha),
            0.7f to color.copy(alpha = 0.95f * maxAlpha),
            0.9f to color.copy(alpha = 1f * maxAlpha)
        )
    )
}

fun LazyGridScope.fullRow(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

/**
 * Checks if the modal is currently expanded or a swipe action is in progress to be expanded.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun SheetState.isFullyExpandedOrTargeted(forceFullExpand: Boolean = false): Boolean {
    val checkState = if (this.hasExpandedState) {
        if (!this.hasPartiallyExpandedState) {
            return false
        }
        SheetValue.Expanded
    } else {
        if (forceFullExpand) {
            return false
        }
        SheetValue.PartiallyExpanded
    }

    return this.targetValue == checkState
}

@Composable
fun SwitchDefaults.alwaysEnabledColors(): SwitchColors {
    val defaultColors = SwitchDefaults.colors()

    return SwitchColors(
        checkedThumbColor = defaultColors.checkedThumbColor,
        checkedTrackColor = defaultColors.checkedTrackColor,
        checkedBorderColor = defaultColors.checkedBorderColor,
        checkedIconColor = defaultColors.checkedIconColor,
        uncheckedThumbColor = defaultColors.uncheckedThumbColor,
        uncheckedTrackColor = defaultColors.uncheckedTrackColor,
        uncheckedBorderColor = defaultColors.uncheckedBorderColor,
        uncheckedIconColor = defaultColors.uncheckedIconColor,
        disabledCheckedThumbColor = defaultColors.checkedThumbColor,
        disabledCheckedTrackColor = defaultColors.checkedTrackColor,
        disabledCheckedBorderColor = defaultColors.checkedBorderColor,
        disabledCheckedIconColor = defaultColors.checkedIconColor,
        disabledUncheckedThumbColor = defaultColors.uncheckedThumbColor,
        disabledUncheckedTrackColor = defaultColors.uncheckedTrackColor,
        disabledUncheckedBorderColor = defaultColors.uncheckedBorderColor,
        disabledUncheckedIconColor = defaultColors.uncheckedIconColor,
    )
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) {
        mutableStateOf(firstVisibleItemIndex)
    }
    var previousScrollOffset by remember(this) {
        mutableStateOf(firstVisibleItemScrollOffset)
    }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

val LazyListState.canScroll: Boolean
    get() = this.canScrollForward || this.canScrollBackward

@Composable
fun LazyListState.scrollUpVisible(): Boolean {
    return if (canScroll) {
        isScrollingUp() && canScrollForward
    } else {
        true
    }
}