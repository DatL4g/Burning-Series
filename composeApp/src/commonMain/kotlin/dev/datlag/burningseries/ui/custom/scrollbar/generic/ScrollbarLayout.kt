package dev.datlag.burningseries.ui.custom.scrollbar.generic

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.datlag.burningseries.ui.custom.scrollbar.foundation.HorizontalScrollbarLayout
import dev.datlag.burningseries.ui.custom.scrollbar.foundation.ScrollbarLayoutSettings
import dev.datlag.burningseries.ui.custom.scrollbar.foundation.VerticalScrollbarLayout

@Composable
internal fun ScrollbarLayout(
    orientation: Orientation,
    thumbSizeNormalized: Float,
    thumbOffsetNormalized: Float,
    thumbIsInAction: Boolean,
    thumbIsSelected: Boolean,
    settings: ScrollbarLayoutSettings,
    draggableModifier: Modifier,
    indicator: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    when (orientation) {
        Orientation.Vertical -> VerticalScrollbarLayout(
            thumbSizeNormalized = thumbSizeNormalized,
            thumbOffsetNormalized = thumbOffsetNormalized,
            thumbIsInAction = thumbIsInAction,
            thumbIsSelected = thumbIsSelected,
            settings = settings,
            draggableModifier = draggableModifier,
            indicator = indicator,
            modifier = modifier,
        )

        Orientation.Horizontal -> HorizontalScrollbarLayout(
            thumbSizeNormalized = thumbSizeNormalized,
            thumbOffsetNormalized = thumbOffsetNormalized,
            thumbIsInAction = thumbIsInAction,
            thumbIsSelected = thumbIsSelected,
            settings = settings,
            draggableModifier = draggableModifier,
            indicator = indicator,
            modifier = modifier,
        )
    }
}