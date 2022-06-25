@file:Obfuscate

package de.datlag.burningseries.common

import android.app.Activity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.michaelrocks.paranoid.Obfuscate

fun Activity.hideKeyboard() = WindowInsetsControllerCompat(this.window, this.window.decorView).hide(WindowInsetsCompat.Type.ime())