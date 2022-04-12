package de.datlag.network.myanimelist

import com.kttdevelopment.mal4j.MyAnimeList
import com.kttdevelopment.mal4j.anime.AnimePreview
import de.datlag.model.JaroWinkler
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max

@Obfuscate
class MyAnimeListRepository @Inject constructor() {

    fun getUserMal(token: String): MyAnimeList {
        return if (token.startsWith("Bearer")) {
            MyAnimeList.withToken(token)
        } else {
            MyAnimeList.withToken("Bearer $token")
        }
    }

    fun getMalSeries(myAnimeList: MyAnimeList, seriesWithInfo: SeriesWithInfo): Flow<AnimePreview?> = flow<AnimePreview?> {
        if (seriesWithInfo.infos.any { info -> info.data.contains("Anime", true) }) {
            coroutineScope {
                val malList = setOf(
                    "${seriesWithInfo.series.title} ${seriesWithInfo.series.season}",
                    seriesWithInfo.series.title,
                    seriesWithInfo.series.hrefTitle
                ).map { async {
                    animePreviewWithDistanceByQuery(
                        myAnimeList,
                        it,
                        seriesWithInfo.series.title,
                        seriesWithInfo.series.hrefTitle
                    )
                } }.awaitAll()

                val bestMatches = malList.maxByOrNull {
                    it.keys.maxOfOrNull { key -> key } ?: 0.0
                }
                val maxEntry = bestMatches?.maxByOrNull { it.key }
                val lenEntry = bestMatches?.minByOrNull { abs(it.value.title.length - seriesWithInfo.series.title.length) }
                val entryDiff = abs((lenEntry?.key ?: Double.MIN_VALUE) - (maxEntry?.key ?: Double.MAX_VALUE))
                val bestEntry = if (entryDiff < 0.15) {
                    lenEntry ?: maxEntry
                } else {
                    maxEntry ?: lenEntry
                }

                if (bestEntry != null && bestEntry.key > 0.65) {
                    emit(bestEntry.value)
                } else {
                    emit(null)
                }
            }
        } else {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun animePreviewWithDistanceByQuery(
        myAnimeList: MyAnimeList,
        query: String,
        seriesTitle: String,
        seriesHrefTitle: String
    ): Map<Double, AnimePreview> {
        return try {
            myAnimeList.anime.includeNSFW().withLimit(15).withQuery(query).search() ?: emptyList()
        } catch (ignored: Exception) { emptyList() }.associateBy {
            max(animePreviewBestDistance(seriesTitle, it), animePreviewBestDistance(seriesHrefTitle, it))
        }
    }

    private fun animePreviewBestDistance(title: String, animePreview: AnimePreview): Double {
        val titleDistance = JaroWinkler.distance(title, animePreview.title ?: String())
        val englishDistance = JaroWinkler.distance(title, animePreview.alternativeTitles.english ?: String())
        val japaneseDistance = JaroWinkler.distance(title, animePreview.alternativeTitles.japanese ?: String())
        val synonyms = animePreview.alternativeTitles.synonyms ?: emptyArray()
        val synonymDistance: Double = if (synonyms.isNotEmpty()) {
            synonyms.map { synonym -> JaroWinkler.distance(title, synonym ?: String()) }.average()
        } else { 0.0 }

        return max(titleDistance, max(englishDistance, max(japaneseDistance, synonymDistance)))
    }
}