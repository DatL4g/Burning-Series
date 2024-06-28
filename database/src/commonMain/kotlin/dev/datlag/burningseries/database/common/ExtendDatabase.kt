package dev.datlag.burningseries.database.common

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.CombinedEpisode
import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.database.ExtendedSeries
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext

fun BurningSeries.isFavorite(seriesData: SeriesData, context: CoroutineContext): Flow<Boolean> {
    return this.burningSeriesQueries.seriesIsFavoriteByHref(seriesData.source).asFlow().mapToOneOrDefault(false, context)
}

fun BurningSeries.isFavoriteOneShot(seriesData: SeriesData): Boolean {
    return this.burningSeriesQueries.seriesIsFavoriteByHref(seriesData.source).executeAsOneOrNull() ?: false
}

fun BurningSeries.setSeriesFavorite(series: Series) {
    val fallbackSeason = if (series.seasons.size == 1) {
        series.seasons.firstOrNull()?.value
    } else {
        null
    }

    this.burningSeriesQueries.upsertSeriesFavoriteSince(
        since = Clock.System.now().epochSeconds,
        hrefPrimary = series.source,
        href = series.href,
        season = series.season ?: fallbackSeason,
        seasons = series.seasons.map { it.value },
        coverHref = series.coverHref,
        fullTitle = series.title,
        mainTitle = series.mainTitle,
        subTitle = series.subTitle,
        isAnime = series.isAnime
    )

    this.burningSeriesQueries.transaction {
        series.episodes.forEach { episode ->
            insertEpisodeOrIgnore(
                episode = episode,
                series = series
            )
        }
    }
}

fun BurningSeries.unsetSeriesFavorite(series: Series) {
    val fallbackSeason = if (series.seasons.size == 1) {
        series.seasons.firstOrNull()?.value
    } else {
        null
    }

    this.burningSeriesQueries.upsertSeriesFavoriteSince(
        since = 0L,
        hrefPrimary = series.source,
        href = series.href,
        season = series.season ?: fallbackSeason,
        seasons = series.seasons.map { it.value },
        coverHref = series.coverHref,
        fullTitle = series.title,
        mainTitle = series.mainTitle,
        subTitle = series.subTitle,
        isAnime = series.isAnime
    )
}

fun BurningSeries.favoritesSeries(context: CoroutineContext): Flow<ImmutableCollection<ExtendedSeries>> {
    return this.burningSeriesQueries.favoriteSeries().asFlow().mapToList(context).map { collection ->
        collection.map(::ExtendedSeries).toImmutableSet()
    }
}

fun BurningSeries.favoritesSeriesOneShot(): ImmutableCollection<ExtendedSeries> {
    return this.burningSeriesQueries.favoriteSeries().executeAsList().map(::ExtendedSeries).toImmutableSet()
}

fun BurningSeries.insertSeriesOrIgnore(series: Series) {
    val fallbackSeason = if (series.seasons.size == 1) {
        series.seasons.firstOrNull()?.value
    } else {
        null
    }

    this.burningSeriesQueries.insertSeriesOrIgnore(
        hrefPrimary = series.source,
        href = series.href,
        season = series.season ?: fallbackSeason,
        seasons = series.seasons.map { it.value },
        coverHref = series.coverHref,
        fullTitle = series.title,
        mainTitle = series.mainTitle,
        subTitle = series.subTitle,
        isAnime = series.isAnime,
        favoriteSince = 0L
    )
}

fun BurningSeries.updateSeriesHref(seriesData: SeriesData, value: String = seriesData.toHref()) {
    this.burningSeriesQueries.updateSeriesHref(
        value = value,
        hrefPrimary = seriesData.source
    )
}

fun BurningSeries.updateSeriesSeason(seriesData: SeriesData, value: Int? = seriesData.season) {
    this.burningSeriesQueries.updateSeriesSeason(
        value = value,
        hrefPrimary = seriesData.source
    )
}

fun BurningSeries.updateSeriesData(seriesData: SeriesData) {
    this.updateSeriesHref(seriesData)
    this.updateSeriesSeason(seriesData)
}

fun BurningSeries.updateLength(value: Long, episode: Series.Episode) {
    this.burningSeriesQueries.updateEpisodeLength(
        value = value,
        href = episode.href
    )
}

fun BurningSeries.updateProgress(value: Long, episode: Series.Episode) {
    this.burningSeriesQueries.updateEpisodeProgress(
        value = value,
        href = episode.href,
        updated = Clock.System.now()
    )
}

fun BurningSeries.insertEpisodeOrIgnore(
    episode: Series.Episode,
    series: Series
) {
    this.insertSeriesOrIgnore(series)

    this.insertEpisodeOrIgnore(
        episode = episode,
        fallbackNumber = series.episodes.indexOf(episode) + 1,
        seriesHref = series.source
    )
}

fun BurningSeries.insertEpisodeOrIgnore(
    episode: Series.Episode,
    fallbackNumber: Int,
    seriesHref: String,
) {
    this.burningSeriesQueries.insertEpisodeOrIgnore(
        href = episode.href,
        number = episode.convertedNumber ?: fallbackNumber,
        title = episode.fullTitle,
        seriesHref = seriesHref,
        updatedAt = Clock.System.now()
    )
}

fun BurningSeries.episodeProgress(episode: Series.Episode, context: CoroutineContext): Flow<Long> {
    return this.burningSeriesQueries.selectEpisodeProgress(episode.href).asFlow().mapToOneOrDefault(0L, context)
}

fun BurningSeries.episodeProgressOneShot(episode: Series.Episode): Long {
    return this.burningSeriesQueries.selectEpisodeProgress(episode.href).executeAsOneOrNull() ?: 0L
}

fun BurningSeries.episodeLengthOneShot(episode: Series.Episode): Long {
    return this.burningSeriesQueries.selectEpisodeLength(episode.href).executeAsOneOrNull() ?: 0L
}

fun BurningSeries.updateEpisodeBlurHash(episode: Series.Episode, hash: String?) {
    this.burningSeriesQueries.updateEpisodeBlurHash(
        value = hash,
        href = episode.href
    )
}

fun BurningSeries.setEpisodeWatched(episode: Series.Episode) {
    this.burningSeriesQueries.episodeMarkWatched(
        href = episode.href,
        updated = Clock.System.now()
    )
}

fun BurningSeries.setEpisodeUnwatched(episode: Series.Episode) {
    this.burningSeriesQueries.episodeMarkUnwatched(
        href = episode.href,
        updated = Clock.System.now()
    )
}

fun BurningSeries.episodeForSeries(series: SeriesData): ImmutableCollection<Episode> {
    return this.burningSeriesQueries.selectEpisodeBySeriesHref(series.source).executeAsList().toImmutableSet()
}

fun BurningSeries.combinedEpisodesForSeries(collection: Collection<Series.Episode>, series: SeriesData): ImmutableCollection<CombinedEpisode> {
    val fromDatabase = episodeForSeries(series)

    return if (fromDatabase.isEmpty()) {
        collection.map(::CombinedEpisode).toImmutableSet()
    } else {
        collection.map { episode ->
            CombinedEpisode(
                default = episode,
                database = fromDatabase.firstOrNull { it.href == episode.href }
            )
        }.toImmutableSet()
    }
}

fun BurningSeries.episodeWatching(episode: Series.Episode, context: CoroutineContext): Flow<Boolean> {
    return this.burningSeriesQueries.selectEpisodeWatchingByHref(episode.href).asFlow().mapToOneOrDefault(false, context)
}

fun BurningSeries.episodeWatchingOneShot(episode: Series.Episode): Boolean {
    return this.burningSeriesQueries.selectEpisodeWatchingByHref(episode.href).executeAsOneOrNull() ?: false
}

fun BurningSeries.episodeFinished(episode: Series.Episode, context: CoroutineContext): Flow<Boolean> {
    return this.burningSeriesQueries.selectEpisodeFinishedByHref(episode.href).asFlow().mapToOneOrDefault(false, context)
}

fun BurningSeries.episodeFinishedOneShot(episode: Series.Episode): Boolean {
    return this.burningSeriesQueries.selectEpisodeFinishedByHref(episode.href).executeAsOneOrNull() ?: false
}

fun BurningSeries.episodeBlurHash(episode: Series.Episode, context: CoroutineContext): Flow<String?> {
    return this.burningSeriesQueries.selectEpisodeBlurHashByHref(episode.href).asFlow().mapToOneOrNull(context).map { it?.blurHash }
}

fun BurningSeries.episodeBlurHashOneShot(episode: Series.Episode): String? {
    return this.burningSeriesQueries.selectEpisodeBlurHashByHref(episode.href).executeAsOneOrNull()?.blurHash
}

fun BurningSeries.episodeNumberOneShot(episode: Series.Episode): Int? {
    return this.burningSeriesQueries.selectEpisodeNumberByHref(episode.href).executeAsOneOrNull()
}

fun BurningSeries.episodeRefreshingData(episode: Series.Episode, context: CoroutineContext): Flow<CombinedEpisode> {
    val databaseEpisode = this@episodeRefreshingData.burningSeriesQueries.selectEpisodeByHref(
        episode.href
    ).executeAsOneOrNull()

    return flow {
        emit(
            CombinedEpisode(
                default = episode,
                database = databaseEpisode
            )
        )
        emitAll(
            combine(
                episodeFinished(episode, context),
                episodeWatching(episode, context),
                episodeProgress(episode, context),
            ) { finished, watching, progress ->
                CombinedEpisode(
                    default = episode,
                    database = databaseEpisode?.copy(
                        watching = watching,
                        finished = finished,
                        progress = progress
                    ) ?: this@episodeRefreshingData.burningSeriesQueries.selectEpisodeByHref(
                        episode.href
                    ).executeAsOneOrNull()
                )
            }
        )
    }
}

fun BurningSeries.seriesFullHref(data: SeriesData): String? {
    return this.burningSeriesQueries.selectSeriesFullHref(data.source).executeAsOneOrNull()?.ifBlank { null }
}

fun BurningSeries.countWatchedEpisodes(): Int {
    return this.burningSeriesQueries.countWatchedEpisode().executeAsOneOrNull()?.toInt() ?: 0
}
