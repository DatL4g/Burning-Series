package dev.datlag.burningseries.other

import android.util.Log

actual object Logger {
    actual fun error(value: String) {
        Log.e("LOGGING", value)
    }
}