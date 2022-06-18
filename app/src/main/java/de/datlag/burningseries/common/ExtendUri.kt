@file:Obfuscate

package de.datlag.burningseries.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import io.michaelrocks.paranoid.Obfuscate

fun Uri?.openInBrowser(context: Context) {
	this ?: return
	
	val browserIntent = Intent(Intent.ACTION_VIEW, this)
	ContextCompat.startActivity(context, browserIntent, null)
}