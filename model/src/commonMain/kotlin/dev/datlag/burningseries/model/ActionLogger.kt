package dev.datlag.burningseries.model

expect class ActionLogger {

    fun reset(newMode: Int? = null)

    fun logInfo(mode: Int, info: String)

    fun logWarning(mode: Int, warn: String)

    fun logError(mode: Int, error: String)

    var requiredMode: Int
}