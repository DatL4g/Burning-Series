package dev.datlag.burningseries.other

import org.jetbrains.skia.impl.Log

actual object Logger {
    actual fun error(value: String) {
        Log.error(value)
    }
}