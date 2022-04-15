package de.datlag.burningseries.ui.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import de.datlag.burningseries.R
import de.datlag.burningseries.common.colorStateListOf
import de.datlag.burningseries.common.getColorCompat


class DefaultTheme : ApplicationTheme {
    override fun defaultBackgroundColor(context: Context): Int {
        return context.getColorCompat(R.color.defaultBackgroundColor)
    }

    override fun defaultContentColor(context: Context): Int {
        return context.getColorCompat(R.color.defaultContentColor)
    }

    override fun switchThumbCheckedColor(context: Context): Int {
        return context.getColorCompat(R.color.datlagColor)
    }

    override fun switchThumbUnCheckedColor(context: Context): Int {
        return context.getColorCompat(R.color.defaultContentColor)
    }

    override fun switchThumbStateList(context: Context): ColorStateList {
        return colorStateListOf(
            intArrayOf(-android.R.attr.state_checked) to switchThumbUnCheckedColor(context),
            intArrayOf(android.R.attr.state_checked) to switchThumbCheckedColor(context),
        )
    }

    override fun switchTrackCheckedColor(context: Context): Int {
        return context.getColorCompat(R.color.datlagColorHalfAlpha)
    }

    override fun switchTrackUnCheckedColor(context: Context): Int {
        return context.getColorCompat(R.color.defaultContentColorHalfAlpha)
    }

    override fun switchTrackStateList(context: Context): ColorStateList {
        return colorStateListOf(
            intArrayOf(-android.R.attr.state_checked) to switchTrackUnCheckedColor(context),
            intArrayOf(android.R.attr.state_checked) to switchTrackCheckedColor(context),
        )
    }

    override fun buttonTransparentEnabledTextColor(context: Context): Int {
        return context.getColorCompat(R.color.defaultBackgroundColor)
    }

    override fun buttonTransparentDisabledTextColor(context: Context): Int {
        return context.getColorCompat(R.color.defaultContentColor)
    }

    override fun buttonTransparentTextStateList(context: Context): ColorStateList {
        return colorStateListOf(
            intArrayOf(-android.R.attr.state_enabled) to buttonTransparentDisabledTextColor(context),
            intArrayOf(android.R.attr.state_enabled) to buttonTransparentEnabledTextColor(context),
        )
    }

    override fun buttonTransparentEnabledBackgroundColor(context: Context): Int {
        return context.getColorCompat(R.color.defaultContentColor)
    }

    override fun buttonTransparentDisabledBackgroundColor(context: Context): Int {
        return Color.TRANSPARENT
    }

    override fun buttonTransparentBackgroundStateList(context: Context): ColorStateList {
        return colorStateListOf(
            intArrayOf(-android.R.attr.state_enabled) to buttonTransparentDisabledBackgroundColor(context),
            intArrayOf(android.R.attr.state_enabled) to buttonTransparentEnabledBackgroundColor(context),
        )
    }

    override fun favoriteIconCheckedColor(context: Context): Int {
        return context.getColorCompat(R.color.datlagColor)
    }

    override fun favoriteIconUnCheckedColor(context: Context): Int {
        return context.getColorCompat(R.color.defaultContentColor)
    }

    override fun playerSeekBarPlayedColor(context: Context): Int {
        return context.getColorCompat(R.color.datlagColor)
    }

    override fun playerSeekBarScrubberColor(context: Context): Int {
        return context.getColorCompat(R.color.datlagColor)
    }

    override fun id(): Int = 0
}