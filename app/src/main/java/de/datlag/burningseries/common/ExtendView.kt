@file:Obfuscate

package de.datlag.burningseries.common

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.GravityInt
import androidx.annotation.LayoutRes
import androidx.core.widget.ImageViewCompat
import androidx.viewbinding.ViewBinding
import io.michaelrocks.paranoid.Obfuscate

val ViewBinding.context: Context
	get() = this.root.context

fun ViewGroup.inflateView(
	@LayoutRes layoutId: Int
) = LayoutInflater.from(this.context).inflate(layoutId, this, false)

fun View.visible() {
	this.visibility = View.VISIBLE
}

fun View.gone() {
	this.visibility = View.GONE
}

fun View.invisible() {
	this.visibility = View.INVISIBLE
}

fun ImageView.clearTint() {
	this.clearColorFilter()
	ImageViewCompat.setImageTintList(this, null)
}

val View.anyWidth: Int?
	get() {
		return when {
			this.width > 0 -> this.width
			this.measuredWidth > 0 -> this.measuredWidth
			this.layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT -> resources?.displayMetrics?.widthPixels
			this.minimumWidth > 0 -> this.minimumWidth
			else -> null
		}
	}

val View.anyHeight: Int?
	get() {
		return when {
			this.height > 0 -> this.height
			this.measuredHeight > 0 -> this.measuredHeight
			this.layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT -> resources?.displayMetrics?.heightPixels
			this.minimumHeight > 0 -> this.minimumHeight
			else -> null
		}
	}

fun View.setLayoutGravity(@GravityInt gravity: Int) {
	val params = this.layoutParams
	val asParams = try {
		params as LinearLayout.LayoutParams
	} catch (ignored: Throwable) {
		try {
			params as FrameLayout.LayoutParams
		} catch (ignored: Throwable) {
			params
		}
	}
	when (asParams) {
		is LinearLayout.LayoutParams -> {
			asParams.gravity = gravity
		}
		is FrameLayout.LayoutParams -> {
			asParams.gravity = gravity
		}
	}
}

fun View.setLayoutGravityCenter() = this.setLayoutGravity(Gravity.CENTER)
fun View.setLayoutGravityNone() = this.setLayoutGravity(Gravity.NO_GRAVITY)
