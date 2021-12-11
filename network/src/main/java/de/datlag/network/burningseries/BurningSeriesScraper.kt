package de.datlag.network.burningseries

import android.util.Log
import androidx.core.text.parseAsHtml
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.AllSeries
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.home.HomeData
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.series.*
import de.datlag.network.common.getHref
import de.datlag.network.common.getSrc
import de.datlag.network.common.getTitle
import de.datlag.network.common.getValue
import io.michaelrocks.paranoid.Obfuscate
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Obfuscate
class BurningSeriesScraper {

    fun scrapeHomeData(): HomeData? {
        val doc: Document = try {
            Jsoup.connect(Constants.API_BS_TO_BASE).get()
        } catch (ignored: Exception) {
            return null
        }

        val latestEpisodesElements: Elements = doc.select("#newest_episodes li")
        val latestSeriesElements: Elements = doc.select("#newest_series li")

        val latestEpisodes: MutableList<LatestEpisode> = mutableListOf()
        val latestSeries: MutableList<LatestSeries> = mutableListOf()

        latestEpisodesElements.forEach {
            val episodeTitle = it.selectFirst("li a")?.getTitle() ?: String()
            val episodeHref = it.selectFirst("li a")?.getHref() ?: String()

            if (episodeTitle.isNotEmpty() && episodeHref.isNotEmpty()) {
                latestEpisodes.add(LatestEpisode(episodeTitle, episodeHref))
            }
        }
        latestSeriesElements.forEach {
            val seriesTitle = it.selectFirst("a")?.getTitle() ?: String()
            val seriesHref = it.selectFirst("a")?.getHref() ?: String()

            if (seriesTitle.isNotEmpty() && seriesHref.isNotEmpty()) {
                latestSeries.add(LatestSeries(seriesTitle, seriesHref))
            }
        }
        return if (latestEpisodes.isNotEmpty() && latestSeries.isNotEmpty()) {
            HomeData(latestEpisodes, latestSeries)
        } else {
            null
        }
    }

    fun scrapeSeriesData(href: String): SeriesData? {
        val url = Constants.getBurningSeriesLink(href)
        Log.e("scrape from", url)
        val doc: Document = try {
            Jsoup.connect(url).timeout(1000 * 60).get()
        } catch (ignored: Exception) {
            return null
        }

        val title = doc.selectFirst(".serie h2")?.wholeText() ?: String()
        val description = doc.selectFirst(".serie p")?.text() ?: String()
        val image = doc.selectFirst(".serie img")?.getSrc() ?: String()
        val seasons: List<String> = doc.select(".serie #seasons ul li").map { it.text() }

        val selectedValue = doc.selectFirst(".series-language option[selected]")?.getValue()
        var selectedLanguage: String? = null
        val languagesElements = doc.select(".series-language > option")
        val languages: MutableList<LanguageData> = mutableListOf()
        languagesElements.forEach {
            val value = it.getValue() ?: String()
            val text = it.text() ?: String()
            val selected = it.selectFirst("option[selected]")?.getValue()
            if (!selected.isNullOrEmpty() || (!selectedValue.isNullOrEmpty() && selectedValue == value)) {
                selectedLanguage = value
            }
            if (value.isNotEmpty() && text.isNotEmpty()) {
                languages.add(LanguageData(value, text))
            }
        }
        if (selectedLanguage.isNullOrEmpty()) {
            selectedLanguage = selectedValue
            if (selectedLanguage.isNullOrEmpty()) {
                selectedLanguage = languages.first().value
            }
        }

        val infoHeaders = doc.select(".serie div > span").map { it.text() }
        val infoData: MutableList<String> = doc.select(".serie div p").map { it.wholeText() }.toMutableList()
        val infoList: MutableList<InfoData> = mutableListOf()

        if (infoData.contains(description)) {
            infoData.removeAt(infoData.indexOf(description))
        } else if (infoData.contains(description.trim())) {
            infoData.removeAt(infoData.indexOf(description.trim()))
        }

        for (i in infoHeaders.indices) {
            val data = if (infoData.size > i && infoData[i].isNotEmpty()) {
                infoData[i].replace(Regex("(?:(\\n)*\\t)+", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)), "\t")
            } else {
                String()
            }

            infoList.add(InfoData(infoHeaders[i], data))
        }

        val episodes = doc.select(".serie .episodes tr")
        val episodeInfoList: MutableList<EpisodeInfo> = mutableListOf()
        episodes.forEach { episodesElement ->
            val episodeData = episodesElement.select("td a")
            val episodeList: MutableList<EpisodeData> = mutableListOf()
            for (i in episodeData.indices) {
                val text = episodeData[i].selectFirst("a")?.text() ?: String()
                val episodeHref = episodeData[i].selectFirst("a")?.getHref() ?: String()
                episodeList.add(EpisodeData(text, episodeHref))
            }

            val episodeHref = when {
                episodeList[0].href.isNotEmpty() -> episodeList[0].href
                episodeList.size > 1 && episodeList[1].href.isNotEmpty() -> episodeList[1].href
                else -> String()
            }
            val hoster = episodeList.map { it.href }.filterNot { it.isEmpty() }.toMutableSet()
            if (hoster.contains(episodeHref)) {
                hoster.remove(episodeHref)
            } else if (hoster.contains(episodeHref.trim())) {
                hoster.remove(episodeHref.trim())
            }

            val hosterList: MutableList<HosterData> = mutableListOf()
            hoster.forEach {
                hosterList.add(HosterData(it.replace(episodeHref, "").replace("/", ""), it))
            }
            val episodeInfo = EpisodeInfo(
                episodeList[0].text.trim(),
                try {
                    episodeList[1].text.trim()
                } catch (ignored: Exception) { String() },
                episodeHref,
                hoster = hosterList
            )
            episodeInfoList.add(episodeInfo)
        }

        val replacedTitle = title.replace(Regex("(?:(\\n)*\\t)+", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)), "\t")
        val splitTitle = replacedTitle.trim().split("\t")
        val normalizedTitle = splitTitle[0].trim()

        val seriesData = SeriesData(
            normalizedTitle,
            try { splitTitle[1].trim() } catch (ignored: Exception) { seasons.firstOrNull() ?: String() },
            description,
            image,
            href = href,
            selectedLanguage = selectedLanguage ?: return null,
            infos = infoList,
            languages = languages,
            seasons = seasons,
            episodes = episodeInfoList
        )

        return if (seriesData.title.isNotEmpty()
            && seriesData.season.isNotEmpty()
            && seriesData.image.isNotEmpty()
            && seriesData.infos.isNotEmpty()
            && seriesData.languages.isNotEmpty()
            && seriesData.seasons.isNotEmpty()
            && seriesData.episodes.isNotEmpty()) {
            seriesData
        } else {
            null
        }
    }

    fun scrapeAllSeries(): List<GenreModel.GenreData> {
        val doc: Document = try {
            Jsoup.connect(Constants.API_BS_TO_ALL).timeout(1000 * 60 * 5).get()
        } catch (ignored: Exception) {
            return emptyList()
        }

        val sections = doc.select(".andere-serien .genre")
        return if (sections.isNullOrEmpty()) {
            emptyList()
        } else {
            val genres: MutableList<GenreModel.GenreData> = mutableListOf()
            sections.forEach { section ->
                val genre = section.selectFirst("strong")?.text() ?: String()
                val items = section.select("li").map { item -> GenreModel.GenreItem(
                    item.selectFirst("a")?.text() ?: String(),
                    item.selectFirst("a")?.getHref() ?: String()
                ) }
                genres.add(GenreModel.GenreData(genre, items = items))
            }

            if (genres.all { it.items.isEmpty() }) {
                emptyList()
            } else {
                genres
            }
        }
    }
}