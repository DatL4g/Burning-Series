@file:Obfuscate

package de.datlag.burningseries.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.core.widget.ImageViewCompat
import androidx.viewbinding.ViewBinding
import io.michaelrocks.paranoid.Obfuscate

val ViewBinding.context: Context
	get() = this.root.context

fun ViewGroup.inflateView(
	@LayoutRes layoutId: Int
) = LayoutInflater.from(this.context).inflate(layoutId, this, false)

fun View.show() {
	this.visibility = View.VISIBLE
}

fun View.hide() {
	this.visibility = View.GONE
}

fun View.invisible() {
	this.visibility = View.INVISIBLE
}

fun ImageView.clearTint() {
	this.clearColorFilter()
	ImageViewCompat.setImageTintList(this, null)
}