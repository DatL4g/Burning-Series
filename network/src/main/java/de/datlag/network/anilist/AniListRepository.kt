package de.datlag.network.anilist

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import de.datlag.model.JaroWinkler
import de.datlag.model.burningseries.series.relation.SeriesWithInfo
import de.datlag.network.anilist.type.MediaFormat
import de.datlag.network.anilist.type.MediaListStatus
import de.datlag.network.anilist.type.MediaType
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
class AniListRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {

    fun getViewer(token: String): Flow<ViewerQuery.Viewer?> = flow {
        emit(try {
            val response = apolloClient
                .newBuilder()
                .addHttpHeader("Authorization", "Bearer $token")
                .build()
                .query(ViewerQuery()).execute()
            response.data?.viewer
        } catch (ignored: Exception) { null })
    }.flowOn(Dispatchers.IO)

    fun getAniListSeries(token: String, seriesWithInfo: SeriesWithInfo): Flow<MediaQuery.Medium?> = flow<MediaQuery.Medium?> {
        if (seriesWithInfo.infos.any { info -> info.data.contains("Anime", true) }) {
            val apolloClientWithToken = apolloClient
                .newBuilder()
                .addHttpHeader("Authorization", "Bearer $token")
                .build()

            coroutineScope {
                val aniList = setOf(
                    "${seriesWithInfo.series.title} ${seriesWithInfo.series.season}",
                    *seriesWithInfo.series.title.split("[|:]".toRegex()).map { "$it ${seriesWithInfo.series.season}".trim() }.toTypedArray(),
                    seriesWithInfo.series.title,
                    *seriesWithInfo.series.title.split("[|:]".toRegex()).map { it.trim() }.toTypedArray(),
                    seriesWithInfo.series.hrefTitle
                ).map { async {
                    animeMediumWithDistance(
                        apolloClientWithToken,
                        it,
                        "${seriesWithInfo.series.title} ${seriesWithInfo.series.season}",
                        seriesWithInfo.series.title,
                        seriesWithInfo.series.hrefTitle
                    )
                } }.awaitAll()

                val bestMatch = aniList.maxByOrNull {
                    it.keys.maxOfOrNull { key -> key } ?: 0.0
                }
                val maxEntry = bestMatch?.maxByOrNull { it.key }
                val lenEntry = bestMatch?.minByOrNull {
                    abs(
                        (it.value.title?.english
                                ?: it.value.title?.romaji
                                ?: it.value.title?.userPreferred
                                ?: it.value.title?.native
                                ?: String()
                            ).length - seriesWithInfo.series.title.length
                    )
                }

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

    private suspend fun animeMediumWithDistance(apolloWithToken: ApolloClient, query: String, seriesTitleWithSeason: String, seriesTitle: String, seriesHrefTitle: String): Map<Double, MediaQuery.Medium> {
        return try {
            apolloWithToken.query(MediaQuery(query, MediaType.ANIME, listOf(MediaFormat.TV, MediaFormat.TV_SHORT, MediaFormat.ONA))).execute().data?.page?.media ?: emptyList()
        } catch (ignored: Exception) { emptyList() }.filterNotNull().associateBy {
            max(mediumBestDistance(seriesTitleWithSeason, it), max(mediumBestDistance(seriesTitle, it), mediumBestDistance(seriesHrefTitle, it)))
        }
    }

    private fun mediumBestDistance(title: String, medium: MediaQuery.Medium): Double {
        val matches = title.split("[|:]".toRegex()).map {
            val englishDistance = JaroWinkler.distance(it.trim(), medium.title?.english ?: String())
            val nativeDistance = JaroWinkler.distance(it.trim(), medium.title?.native ?: String())
            val romajiDistance = JaroWinkler.distance(it.trim(), medium.title?.romaji ?: String())
            val userPreferredDistance = JaroWinkler.distance(it.trim(), medium.title?.userPreferred ?: String())
            val synonymDistance: Double = if (!medium.synonyms.isNullOrEmpty()) {
                medium.synonyms.map { synonym -> JaroWinkler.distance(it.trim(), synonym ?: String()) }.average()
            } else { 0.0 }

            max(englishDistance, max(nativeDistance, max(romajiDistance, max(userPreferredDistance, synonymDistance))))
        }.toMutableList()

        if (matches.size > 1) {
            val englishDistance = JaroWinkler.distance(title, medium.title?.english ?: String())
            val nativeDistance = JaroWinkler.distance(title, medium.title?.native ?: String())
            val romajiDistance = JaroWinkler.distance(title, medium.title?.romaji ?: String())
            val userPreferredDistance = JaroWinkler.distance(title, medium.title?.userPreferred ?: String())
            val synonymDistance: Double = if (!medium.synonyms.isNullOrEmpty()) {
                medium.synonyms.map { synonym -> JaroWinkler.distance(title, synonym ?: String()) }.average()
            } else { 0.0 }

            matches.add(max(englishDistance, max(nativeDistance, max(romajiDistance, max(userPreferredDistance, synonymDistance)))))
        }

        return matches.average()
    }

    suspend fun saveAniListEntry(token: String, id: Int, progress: Int, status: MediaListStatus) {
        try {
            apolloClient
                .newBuilder()
                .addHttpHeader("Authorization", "Bearer $token")
                .build()
                .mutation(SaveMediaMutation(id, progress, status)).execute()
        } catch (ignored: Exception) { }
    }
}