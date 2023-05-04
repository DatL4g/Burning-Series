package dev.datlag.burningseries.model

import java.io.File

actual class ActionLogger(private val file: File) {

    actual var requiredMode: Int = 0

    actual fun reset(newMode: Int?) {
        file.writeText(String())
        newMode?.let {
            requiredMode = newMode
        }
    }

    actual fun logInfo(mode: Int, info: String) {
        if (mode == requiredMode) {
            file.appendText("[INFO] $info\n")
        }
    }

    actual fun logWarning(mode: Int, warn: String) {
        if (mode == requiredMode) {
            file.appendText("[WARNING] $warn\n")
        }
    }

    actual fun logError(mode: Int, error: String) {
        if (mode == requiredMode) {
            file.appendText("[ERROR] $error\n")
        }
    }

}