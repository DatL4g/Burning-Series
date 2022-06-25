@file:Obfuscate

package de.datlag.burningseries.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.datlag.burningseries.R
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.helper.AlertDialogButtonIcon
import io.michaelrocks.paranoid.Obfuscate

fun MaterialAlertDialogBuilder.setButtonIcons(
    positive: AlertDialogButtonIcon?,
    negative: AlertDialogButtonIcon?,
    neutral: AlertDialogButtonIcon?
): AlertDialog {
    fun resizeIcon(button: Button, iconInfo: AlertDialogButtonIcon?): Drawable? {
        return iconInfo?.icon?.apply {
            val iconSize = iconInfo.size ?: if (iconInfo.useTextSize) {
                button.textSize.toInt()
            } else {
                button.context.resources.getDimension(R.dimen.materialAlertDialogButtonIconSize).toInt()
            }
            setBounds(0, 0, iconSize, iconSize)
            if (iconInfo.useTextColor) {
                clearColorFilter()
                colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(button.currentTextColor, iconInfo.blendMode)
            }
        }
    }

    fun setButtonIcon(button: Button, iconInfo: AlertDialogButtonIcon?) {
        val resizedIcon = resizeIcon(button, iconInfo)
        button.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        button.setCompoundDrawables(
            if (iconInfo?.gravity == AlertDialogButtonIcon.Gravity.LEFT) {
                resizedIcon
            } else {
                null
            },
            if (iconInfo?.gravity == AlertDialogButtonIcon.Gravity.TOP) {
                resizedIcon
            } else {
                null
            },
            if (iconInfo?.gravity == AlertDialogButtonIcon.Gravity.RIGHT) {
                resizedIcon
            } else {
                null
            },
            if (iconInfo?.gravity == AlertDialogButtonIcon.Gravity.BOTTOM) {
                resizedIcon
            } else {
                null
            }
        )
    }

    val alertDialog = this.create()
    alertDialog.setOnShowListener {
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        setButtonIcon(positiveButton, positive)

        val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        setButtonIcon(negativeButton, negative)

        val neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        setButtonIcon(neutralButton, neutral)
    }
    return alertDialog
}

fun MaterialAlertDialogBuilder.setButtonIcons(
    positive: Drawable?,
    negative: Drawable?,
    neutral: Drawable?
) = this.setButtonIcons(AlertDialogButtonIcon(positive), AlertDialogButtonIcon(negative), AlertDialogButtonIcon(neutral))

class MaterialDialogBuilder(
    private val context: Context,
    private val overrideThemeResId: Int = 0
) {
    private var materialDialogBuilder: MaterialAlertDialogBuilder? = null
    private var positiveButtonIcon: AlertDialogButtonIcon? = null
    private var negativeButtonIcon: AlertDialogButtonIcon? = null
    private var neutralButtonIcon: AlertDialogButtonIcon? = null

    fun setPositiveButtonIcon(icon: AlertDialogButtonIcon) = apply {
        positiveButtonIcon = icon
    }

    fun setPositiveButtonIcon(icon: Drawable?) = apply {
        positiveButtonIcon?.let {
            it.icon = icon
        } ?: run { positiveButtonIcon = AlertDialogButtonIcon(icon = icon) }
    }

    fun setPositiveButtonIcon(@DrawableRes resId: Int) = setPositiveButtonIcon(ContextCompat.getDrawable(context, resId))

    fun setPositiveButtonIconSize(size: Int) = apply {
        positiveButtonIcon?.let {
            it.size = size
        } ?: run { positiveButtonIcon = AlertDialogButtonIcon(icon = null, size = size) }
    }

    fun setPositiveButtonIconGravity(gravity: AlertDialogButtonIcon.Gravity) = apply {
        positiveButtonIcon?.let {
            it.gravity = gravity
        } ?: run { positiveButtonIcon = AlertDialogButtonIcon(icon = null, gravity = gravity) }
    }

    fun usePositiveButtonIconTextColor(`do`: Boolean = true) = apply {
        positiveButtonIcon?.let {
            it.useTextColor = `do`
        } ?: run { positiveButtonIcon = AlertDialogButtonIcon(icon = null, useTextColor = `do`) }
    }

    fun usePositiveButtonIconTextSize(`do`: Boolean = true) = apply {
        positiveButtonIcon?.let {
            it.useTextSize = `do`
        } ?: run { positiveButtonIcon = AlertDialogButtonIcon(icon = null, useTextSize = `do`) }
    }

    fun setPositiveButtonIconBlendMode(mode: BlendModeCompat) = apply {
        positiveButtonIcon?.let {
            it.blendMode = mode
        } ?: run { positiveButtonIcon = AlertDialogButtonIcon(icon = null, blendMode = mode) }
    }

    fun setNegativeButtonIcon(icon: AlertDialogButtonIcon) = apply {
        negativeButtonIcon = icon
    }

    fun setNegativeButtonIcon(icon: Drawable?) = apply {
        negativeButtonIcon?.let {
            it.icon = icon
        } ?: run { negativeButtonIcon = AlertDialogButtonIcon(icon = icon) }
    }

    fun setNegativeButtonIcon(@DrawableRes resId: Int) = setNegativeButtonIcon(ContextCompat.getDrawable(context, resId))

    fun setNegativeButtonIconSize(size: Int) = apply {
        negativeButtonIcon?.let {
            it.size = size
        } ?: run { negativeButtonIcon = AlertDialogButtonIcon(icon = null, size = size) }
    }

    fun setNegativeButtonIconGravity(gravity: AlertDialogButtonIcon.Gravity) = apply {
        negativeButtonIcon?.let {
            it.gravity = gravity
        } ?: run { negativeButtonIcon = AlertDialogButtonIcon(icon = null, gravity = gravity) }
    }

    fun useNegativeButtonIconTextColor(`do`: Boolean = true) = apply {
        negativeButtonIcon?.let {
            it.useTextColor = `do`
        } ?: run { negativeButtonIcon = AlertDialogButtonIcon(icon = null, useTextColor = `do`) }
    }

    fun useNegativeButtonIconTextSize(`do`: Boolean = true) = apply {
        negativeButtonIcon?.let {
            it.useTextSize = `do`
        } ?: run { negativeButtonIcon = AlertDialogButtonIcon(icon = null, useTextSize = `do`) }
    }

    fun setNegativeButtonIconBlendMode(mode: BlendModeCompat) = apply {
        negativeButtonIcon?.let {
            it.blendMode = mode
        } ?: run { negativeButtonIcon = AlertDialogButtonIcon(icon = null, blendMode = mode) }
    }

    fun setNeutralButtonIcon(icon: AlertDialogButtonIcon) = apply {
        neutralButtonIcon = icon
    }

    fun setNeutralButtonIcon(icon: Drawable?) = apply {
        neutralButtonIcon?.let {
            it.icon = icon
        } ?: run { neutralButtonIcon = AlertDialogButtonIcon(icon = icon) }
    }

    fun setNeutralButtonIcon(@DrawableRes resId: Int) = setNeutralButtonIcon(ContextCompat.getDrawable(context, resId))

    fun setNeutralButtonIconSize(size: Int) = apply {
        neutralButtonIcon?.let {
            it.size = size
        } ?: run { neutralButtonIcon = AlertDialogButtonIcon(icon = null, size = size) }
    }

    fun setNeutralButtonIconGravity(gravity: AlertDialogButtonIcon.Gravity) = apply {
        neutralButtonIcon?.let {
            it.gravity = gravity
        } ?: run { neutralButtonIcon = AlertDialogButtonIcon(icon = null, gravity = gravity) }
    }

    fun useNeutralButtonIconTextColor(`do`: Boolean = true) = apply {
        neutralButtonIcon?.let {
            it.useTextColor = `do`
        } ?: run { neutralButtonIcon = AlertDialogButtonIcon(icon = null, useTextColor = `do`) }
    }

    fun useNeutralButtonIconTextSize(`do`: Boolean = true) = apply {
        neutralButtonIcon?.let {
            it.useTextSize = `do`
        } ?: run { neutralButtonIcon = AlertDialogButtonIcon(icon = null, useTextSize = `do`) }
    }

    fun setNeutralButtonIconBlendMode(mode: BlendModeCompat) = apply {
        neutralButtonIcon?.let {
            it.blendMode = mode
        } ?: run { neutralButtonIcon = AlertDialogButtonIcon(icon = null, blendMode = mode) }
    }


    fun builder(dialogBuilder: MaterialAlertDialogBuilder.() -> Unit) = apply {
        materialDialogBuilder = MaterialAlertDialogBuilder(context, overrideThemeResId).apply(dialogBuilder)
    }

    fun build() = (materialDialogBuilder ?: MaterialAlertDialogBuilder(context, overrideThemeResId)).setButtonIcons(positiveButtonIcon, negativeButtonIcon, neutralButtonIcon)
}

fun Context.materialDialogBuilder(builder: MaterialDialogBuilder.() -> Unit) = MaterialDialogBuilder(this).apply(builder).build()
fun Context.materialDialogBuilder(overrideThemeResId: Int, builder: MaterialDialogBuilder.() -> Unit) = MaterialDialogBuilder(this, overrideThemeResId).apply(builder).build()

fun AdvancedFragment.materialDialogBuilder(builder: MaterialDialogBuilder.() -> Unit) = MaterialDialogBuilder(this.safeContext).apply(builder).build()
fun AdvancedFragment.materialDialogBuilder(overrideThemeResId: Int, builder: MaterialDialogBuilder.() -> Unit) = MaterialDialogBuilder(this.safeContext, overrideThemeResId).apply(builder).build()
