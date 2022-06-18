@file:Obfuscate

package de.datlag.burningseries.common

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.devs.readmoreoption.ReadMoreOption
import com.google.android.material.snackbar.Snackbar
import de.datlag.burningseries.R
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import kotlin.math.round

fun Context.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)

fun Context.readMoreOption(builder: ReadMoreOption.Builder.() -> Unit) = ReadMoreOption.Builder(this).apply(builder).build()

tailrec fun Context.getActivity(): Activity? = this as? Activity? ?: (this as? ContextWrapper?)?.baseContext?.getActivity()

tailrec fun Context.getLifecycleOwner(): LifecycleOwner? = this as? LifecycleOwner ?: (this as? ContextWrapper?)?.baseContext?.getLifecycleOwner()

fun Resources.dpToPx(value: Number) = round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), this.displayMetrics))

fun Context.dpToPx(value: Number) = this.resources.dpToPx(value)

fun colorStateListOf(vararg mapping: Pair<IntArray, Int>): ColorStateList {
    val (states, colors) = mapping.unzip()
    return ColorStateList(states.toTypedArray(), colors.toIntArray())
}

fun Context.copyToClipboard(description: String, text: String) {
    val clipBoardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
    clipBoardManager?.setPrimaryClip(ClipData.newPlainText(description, text))
}

fun Context.isInstalledFromFDroid(): Boolean {
    fun installerPackage(): String? {
        return try {
            this.packageManager.getInstallerPackageName(this.packageName)
        } catch (ignored: Exception) {
            null
        }
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            val sourceInfo = this.packageManager.getInstallSourceInfo(this.packageName)
            when {
                sourceInfo.initiatingPackageName?.equals(Constants.F_DROID_PACKAGE_NAME, true) == true -> true
                sourceInfo.originatingPackageName?.equals(Constants.F_DROID_PACKAGE_NAME, true) == true -> true
                sourceInfo.installingPackageName?.equals(Constants.F_DROID_PACKAGE_NAME, true) == true -> true
                else -> false
            }
        } catch (ignored: Exception) {
            installerPackage()?.equals(Constants.F_DROID_PACKAGE_NAME, true) ?: false
        }
    } else {
        installerPackage()?.equals(Constants.F_DROID_PACKAGE_NAME, true) ?: false
    }
}

fun Context.errorSnackbar(view: View, text: CharSequence, duration: Int): Snackbar {
    val themeWrapper = ContextThemeWrapper(this, R.style.MaterialErrorSnackbarTheme)
    return Snackbar.make(themeWrapper, view, text, duration)
}

fun Context.errorSnackbar(view: View, @StringRes resId: Int, duration: Int): Snackbar {
    return this.errorSnackbar(view, this.getString(resId), duration)
}

fun Context.warningSnackbar(view: View, text: CharSequence, duration: Int): Snackbar {
    val themeWrapper = ContextThemeWrapper(this, R.style.MaterialWarningSnackbarTheme)
    return Snackbar.make(themeWrapper, view, text, duration)
}

fun Context.warningSnackbar(view: View, @StringRes resId: Int, duration: Int): Snackbar {
    return this.warningSnackbar(view, this.getString(resId), duration)
}
