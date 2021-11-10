@file:Obfuscate

package de.datlag.burningseries.common

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import de.datlag.burningseries.extend.AdvancedFragment
import io.michaelrocks.paranoid.Obfuscate
import java.io.File

val Fragment.safeContext: Context
	get() = this.context ?: this.safeActivity ?: if (this is AdvancedFragment) appContext else requireContext()

val Fragment.safeActivity: Activity?
	get() {
		return when {
			this.activity != null -> this.activity
			this.parentFragment != null -> this.requireParentFragment().safeActivity
			else -> null
		}
	}

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

fun Fragment.loadFileInternal(name: String): ByteArray? {
	return try {
		safeActivity?.let {
			val imageFile = File(it.filesDir, name)
			if (imageFile.exists() && imageFile.isFile && imageFile.canRead()) {
				imageFile.readBytes()
			} else {
				null
			}
		} ?: run { null }
	} catch (ignored: Exception) {
		null
	}
}