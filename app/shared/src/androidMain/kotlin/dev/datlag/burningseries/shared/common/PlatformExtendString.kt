package dev.datlag.burningseries.shared.common

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dev.datlag.burningseries.model.common.scopeCatching

fun String.openInBrowser(context: Context) {
    val browserIntent = Intent(Intent.ACTION_VIEW, this.toUri())

    if (scopeCatching {
        ContextCompat.startActivity(context, browserIntent, null)
    }.isSuccess) {
        return
    }

    val newIntent = browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    scopeCatching {
        ContextCompat.startActivity(context, newIntent, null)
    }
}