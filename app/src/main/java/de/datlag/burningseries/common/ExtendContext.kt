@file:Obfuscate

package de.datlag.burningseries.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.devs.readmoreoption.ReadMoreOption
import io.michaelrocks.paranoid.Obfuscate

fun Context.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)

fun Context.readMoreOption(builder: ReadMoreOption.Builder.() -> Unit) = ReadMoreOption.Builder(this).apply(builder).build()

tailrec fun Context.getActivity(): Activity? = this as? Activity? ?: (this as? ContextWrapper?)?.baseContext?.getActivity()

tailrec fun Context.getLifecycleOwner(): LifecycleOwner? = this as? LifecycleOwner ?: (this as? ContextWrapper?)?.baseContext?.getLifecycleOwner()