package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.Series
import io.ktor.client.*

expect object BsScraper {

    fun logger(logger: ActionLogger): BsScraper

    fun client(client: HttpClient): BsScraper

    suspend fun getHome(url: String?): Home?

    suspend fun getAll(url: String?): List<Genre>?

    suspend fun getSeries(url: String?): Series?
}