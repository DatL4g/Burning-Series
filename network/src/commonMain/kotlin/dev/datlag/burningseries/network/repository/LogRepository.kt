package dev.datlag.burningseries.network.repository

import dev.datlag.burningseries.model.ActionLogger

interface LogRepository {

    val mode: Int

    val logger: ActionLogger

    fun info(info: String) = logger.logInfo(mode, info)
    fun warning(waring: String) = logger.logWarning(mode, waring)
    fun error(error: String) = logger.logError(mode, error)
}