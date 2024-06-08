package dev.datlag.burningseries.ui.custom.video

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout

sealed interface ResizeMode {
    val rawValue: String

    /**
     * Convert [ResizeMode] to [AspectRatioFrameLayout.ResizeMode] resize mode.
     *
     * @return [AspectRatioFrameLayout.RESIZE_MODE_FIT] or [AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH]
     * or [AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT] or [AspectRatioFrameLayout.RESIZE_MODE_FILL]
     * or [AspectRatioFrameLayout.RESIZE_MODE_ZOOM]
     */
    @OptIn(UnstableApi::class)
    fun toPlayerViewResizeMode(): Int = when (this) {
        is Fit -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        is FixedWidth -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        is FixedHeight -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
        is Fill -> AspectRatioFrameLayout.RESIZE_MODE_FILL
        is Zoom -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    }

    data object Fit : ResizeMode {
        override val rawValue: String = "fit"
    }

    data object FixedWidth : ResizeMode {
        override val rawValue: String = "fixed_width"
    }

    data object FixedHeight : ResizeMode {
        override val rawValue: String = "fixed_height"
    }

    data object Fill : ResizeMode {
        override val rawValue: String = "fill"
    }

    data object Zoom : ResizeMode {
        override val rawValue: String = "zoom"
    }

    companion object {

        /**
         * Convert [AspectRatioFrameLayout.ResizeMode] resize mode to [ResizeMode].
         *
         * @return [ResizeMode.Fit] or [ResizeMode.FixedWidth] or [ResizeMode.FixedHeight]
         * or [ResizeMode.Fill] or [ResizeMode.Zoom]
         */
        @OptIn(UnstableApi::class)
        fun fromInt(value: Int): ResizeMode = when (value) {
            AspectRatioFrameLayout.RESIZE_MODE_FIT -> Fit
            AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH -> FixedWidth
            AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT -> FixedHeight
            AspectRatioFrameLayout.RESIZE_MODE_FILL -> Fill
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> Zoom
            else -> Fit
        }
    }
}