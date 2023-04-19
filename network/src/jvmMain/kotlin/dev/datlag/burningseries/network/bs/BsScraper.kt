package dev.datlag.burningseries.network.bs

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

    actual suspend fun getHome(url: String?): Home? {
        return try {
            if (url != null) {
                JvmBsScraper.getDocument(url)?.let {
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
                JvmBsScraper.getDocument(url)?.let {
                    JvmBsScraper.getAllSeries(it)
                }
            } else {
                null
            }
        } catch (ignored: Throwable) {
            null
        }
    }

    actual suspend fun getSeries(url: String?, client: HttpClient): Series? {
        return try {
            if (url != null) {
                JvmBsScraper.getDocument(url)?.let {
                    JvmBsScraper.getSeries(it, client)
                }
            } else {
                null
            }
        } catch (ignored: Throwable) {
            null
        }
    }
}