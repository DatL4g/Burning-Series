@file:Obfuscate

package de.datlag.burningseries.common

import android.app.Activity
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import de.datlag.burningseries.R
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.ui.dialog.LoadingDialog
import io.michaelrocks.paranoid.Obfuscate
import java.io.File

val Fragment.safeContext: Context
	get() = this.context ?: this.safeActivity ?: if (this is AdvancedFragment) appContext else requireContext()

val Fragment.safeActivity: Activity?
	get() {
		return when {
			this.activity != null -> this.activity
			this.parentFragment != null -> this.requireParentFragment().safeActivity
			else -> this.context?.getActivity()
		}
	}

val Fragment.isTelevision: Boolean
	get() = safeContext.packageManager.isTelevision()

fun Fragment.getCompatColor(@ColorRes resId: Int) = ContextCompat.getColor(safeContext, resId)
fun Fragment.getCompatDrawable(@DrawableRes resId: Int) = ContextCompat.getDrawable(safeContext, resId)

fun Fragment.saveFileInternal(name: String, bytes: ByteArray): Boolean {
	return try {
		safeActivity?.let {
			it.openFileOutput(name, Context.MODE_PRIVATE).use { stream ->
				stream.write(bytes)
				stream.flush()
			}
			true
		} ?: run { false }
	} catch (ignored: Exception) {
		false
	}
}

fun Fragment.checkFileValid(name: String): Boolean {
	return try {
	    safeActivity?.let {
	    	val imageFile = File(it.filesDir, name)
			imageFile.exists() && imageFile.isFile && imageFile.canRead()
		} ?: run { false }
	} catch (ignored: Exception) {
		false
	}
}

fun Fragment.loadFileInternal(name: String): ByteArray? {
	return try {
		safeActivity?.let {
			val imageFile = File(it.filesDir, name)
			if (checkFileValid(name)) {
				imageFile.readBytes()
			} else {
				null
			}
		} ?: run { null }
	} catch (ignored: Exception) {
		null
	}
}

fun Fragment.isOrientation(orientation: Int) = safeContext.resources.configuration.orientation == orientation

fun Fragment.getThemedLayoutInflater(
	inflater: LayoutInflater = this.layoutInflater,
	@StyleRes themeResId: Int = R.style.AppTheme
): LayoutInflater {
	if (themeResId == 0) {
		return inflater
	}

	val contextThemeWrapper = ContextThemeWrapper(safeContext, themeResId)
	safeContext.theme.applyStyle(themeResId, true)
	return inflater.cloneInContext(contextThemeWrapper)
}

fun Fragment.showLoadingDialog() = LoadingDialog.show(safeContext)
fun Fragment.hideLoadingDialog() = LoadingDialog.dismiss()

fun Fragment.hideKeyboard() = safeActivity?.hideKeyboard()