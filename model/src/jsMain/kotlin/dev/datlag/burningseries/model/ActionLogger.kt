package dev.datlag.burningseries.model

actual class ActionLogger {

    actual var requiredMode: Int = 0

    actual fun reset(newMode: Int?) {
        console.asDynamic().clear()
        newMode?.let {
            requiredMode = newMode
        }
    }

    actual fun logInfo(mode: Int, info: String) {
        console.log(info)
    }

    actual fun logWarning(mode: Int, warn: String) {
        console.warn(warn)
    }

    actual fun logError(mode: Int, error: String) {
        console.error(error)
    }

}