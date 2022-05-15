package de.datlag.network.burningseries

import android.util.Log
import de.datlag.model.Constants
import de.datlag.model.burningseries.allseries.GenreModel
import de.datlag.model.burningseries.home.HomeData
import de.datlag.model.burningseries.home.LatestEpisode
import de.datlag.model.burningseries.home.LatestEpisodeInfoFlags
import de.datlag.model.burningseries.home.LatestSeries
import de.datlag.model.burningseries.series.*
import de.datlag.network.common.getHref
import de.datlag.network.common.getSrc
import de.datlag.network.common.getTitle
import de.datlag.network.common.getValue
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.datetime.Clock
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

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
            val episodeInfo = it.selectFirst("li .info")?.text() ?: String()
            val episodeInfoFlagsElements = it.select("li .info i")
            val episodeInfoFlags: MutableList<LatestEpisodeInfoFlags> = mutableListOf()
            episodeInfoFlagsElements.forEach { infoFlags ->
                val flagClass = infoFlags.selectFirst("i")?.className() ?: String()
                val flagTitle = infoFlags.selectFirst("i")?.getTitle() ?: String()
                episodeInfoFlags.add(LatestEpisodeInfoFlags(flagClass, flagTitle))
            }

            if (episodeTitle.isNotEmpty() && episodeHref.isNotEmpty()) {
                latestEpisodes.add(LatestEpisode(
                    episodeTitle,
                    episodeHref,
                    episodeInfo,
                    Clock.System.now().epochSeconds,
                    episodeInfoFlags
                ))
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
        val allImages = doc.select(".serie img")

        val coverImage = (allImages.firstOrNull {
            it.hasAttr("alt") && it.attr("alt").equals("Cover", true)
        } ?: allImages.firstOrNull())?.getSrc() ?: return null

        val seasons: List<SeasonData> = doc.select(".serie #seasons ul li").mapIndexed { index, it ->
            val seasonTitle = it.selectFirst("a")?.text() ?: it.text()
            val link = it.selectFirst("a")?.getHref() ?: it.getHref()
            val value: Int = if (link.isNullOrEmpty()) {
                index
            } else {
                if (link.split('/').size >= 3) {
                    link.split('/')[2].toIntOrNull() ?: index
                } else {
                    index
                }
            }
            SeasonData(seasonTitle, value)
        }

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
                selectedLanguage = languages.firstOrNull()?.value ?: return null
            }
        }

        val infoHeaders = doc.select(".serie .infos div > span").map { it.text() }
        val infoData: MutableList<String> = doc.select(".serie .infos p").map { it.wholeText() }.toMutableList()
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
            episodeData.forEach { data ->
                val text = data.selectFirst("a")?.text() ?: String()
                val episodeHref = data.selectFirst("a")?.getHref() ?: String()
                episodeList.add(EpisodeData(text, episodeHref))
            }

            val episodeHref = when {
                episodeList.size <= 0 -> return null
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
            if (splitTitle.size >= 2) splitTitle[1].trim() else seasons.firstOrNull()?.title ?: String(),
            description,
            coverImage,
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