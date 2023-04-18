package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.Series
import io.ktor.client.*

actual object BsScraper {
    actual suspend fun getHome(url: String?): Home? {
        return null
    }

    actual suspend fun getAll(url: String?): List<Genre>? {
        return null
    }

    actual suspend fun getSeries(
        url: String?,
        client: HttpClient
    ): Series? {
        return null
    }

}