@file:Obfuscate

package de.datlag.burningseries.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import io.michaelrocks.paranoid.Obfuscate

val ViewBinding.context: Context
	get() = this.root.context

fun ViewGroup.inflateView(
	@LayoutRes layoutId: Int
) = LayoutInflater.from(this.context).inflate(layoutId, this, false)