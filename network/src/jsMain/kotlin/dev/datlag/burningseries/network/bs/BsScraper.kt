package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.Series
import io.ktor.client.*
import io.ktor.util.logging.*

actual object BsScraper {

    actual fun client(client: HttpClient): BsScraper = apply {  }

    actual fun logger(logger: ActionLogger): BsScraper = apply {  }

    actual suspend fun getHome(url: String?): Home? {
        return null
    }

    actual suspend fun getAll(url: String?): List<Genre>? {
        return null
    }

    actual suspend fun getSeries(url: String?): Series? {
        return null
    }

}