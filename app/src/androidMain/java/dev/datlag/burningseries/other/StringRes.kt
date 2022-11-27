package dev.datlag.burningseries.other

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dev.datlag.burningseries.R

actual class StringRes(private val context: Context) {

    actual val appName: String
        get() = context.getString(R.string.app_name)

    actual val login: String
        get() = context.getString(R.string.login)

    actual val episodes: String
        get() = context.getString(R.string.episodes)

    actual val seriesSingular: String
        get() = context.getString(R.string.series_singular)

    actual val seriesPlural: String
        get() = context.getString(R.string.series_plural)

    actual val githubRepository: String
        get() = context.getString(R.string.github_repository)

    actual val more: String
        get() = context.getString(R.string.more)

    actual val settings: String
        get() = context.getString(R.string.settings)

    actual val showPassword: String
        get() = context.getString(R.string.show_password)

    actual val hidePassword: String
        get() = context.getString(R.string.hide_password)

    actual fun openInBrowser(url: String): Boolean {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        if (runCatching {
            ContextCompat.startActivity(context, browserIntent, null)
        }.isSuccess) {
            return true
        }

        val newIntent = browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            ContextCompat.startActivity(context, newIntent, null)
        }.isSuccess
    }
}