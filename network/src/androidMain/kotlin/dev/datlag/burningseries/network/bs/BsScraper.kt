package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.*
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
                JvmBsScraper.getDocument(url, LoggingMode.HOME)?.let {
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
                JvmBsScraper.getDocument(url, LoggingMode.SEARCH)?.let {
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
                val href = BSUtil.fixSeriesHref(url)
                JvmBsScraper.getDocument(href, LoggingMode.SERIES)?.let {
                    JvmBsScraper.getSeries(it, href)
                }
            } else {
                null
            }
        } catch (ignored: Throwable) {
            null
        }
    }

}