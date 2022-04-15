package de.datlag.burningseries.ui.theme

import android.content.Context
import android.content.res.ColorStateList
import com.dolatkia.animatedThemeManager.AppTheme

interface ApplicationTheme : AppTheme {

    fun defaultBackgroundColor(context: Context): Int
    fun defaultContentColor(context: Context): Int

    fun switchThumbCheckedColor(context: Context): Int
    fun switchThumbUnCheckedColor(context: Context): Int
    fun switchThumbStateList(context: Context): ColorStateList
    fun switchTrackCheckedColor(context: Context): Int
    fun switchTrackUnCheckedColor(context: Context): Int
    fun switchTrackStateList(context: Context): ColorStateList


    fun buttonTransparentEnabledTextColor(context: Context): Int
    fun buttonTransparentDisabledTextColor(context: Context): Int
    fun buttonTransparentTextStateList(context: Context): ColorStateList

    fun buttonTransparentEnabledBackgroundColor(context: Context): Int
    fun buttonTransparentDisabledBackgroundColor(context: Context): Int
    fun buttonTransparentBackgroundStateList(context: Context): ColorStateList

    fun favoriteIconCheckedColor(context: Context): Int
    fun favoriteIconUnCheckedColor(context: Context): Int

    fun playerSeekBarPlayedColor(context: Context): Int
    fun playerSeekBarScrubberColor(context: Context): Int

}