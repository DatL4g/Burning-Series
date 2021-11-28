@file:Obfuscate

package de.datlag.burningseries.common

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.devs.readmoreoption.ReadMoreOption
import io.michaelrocks.paranoid.Obfuscate

fun Context.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)

fun Context.readMoreOption(builder: ReadMoreOption.Builder.() -> Unit) = ReadMoreOption.Builder(this).apply(builder).build()