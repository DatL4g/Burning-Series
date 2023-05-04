package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.scraper.bs.JvmBsScraper
import io.ktor.client.*

actual object BsScraper {

    init {
        JvmBsScraper.coverBlock = { cover, isNsfw ->
            CoverScraper.getCover(cover, isNsfw)
        }
    }

    actual fun client(client: HttpClient) = apply {
        JvmBsScraper.client(client)
    }

    actual fun logger(logger: ActionLogger) = apply {
        JvmBsScraper.logger(logger)
    }

    actual suspend fun getHome(url: String?): Home? {
        return try {
            if (url != null) {
                JvmBsScraper.getDocument(url, 1)?.let {
                    Home(
                        episodes = JvmBsScraper.getLatestEpisodes(it),
                        series = JvmBsScraper.getLatestSeries(it)
                    )
                }
            } else {
                null
            }
        } catch (ignored: Throwable) {
            null
        }
    }

    actual suspend fun getAll(url: String?): List<Genre>? {
        return try {
            if (url != null) {
                JvmBsScraper.getDocument(url, 2)?.let {
                    JvmBsScraper.getAllSeries(it)
                }
            } else {
                null
            }
        } catch (ignored: Throwable) {
            null
        }
    }

    actual suspend fun getSeries(url: String?): Series? {
        return try {
            if (url != null) {
                JvmBsScraper.getDocument(JvmBsScraper.fixSeriesHref(url), 3)?.let {
                    JvmBsScraper.getSeries(it)
                }
            } else {
                null
            }
        } catch (ignored: Throwable) {
            null
        }
    }

}