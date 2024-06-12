package dev.datlag.burningseries.database.common

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext

fun BurningSeries.isFavorite(seriesData: SeriesData, context: CoroutineContext): Flow<Boolean> {
    return this.burningSeriesQueries.seriesIsFavoriteByHref(seriesData.source).asFlow().mapToOneOrDefault(false, context)
}

fun BurningSeries.isFavoriteOneShot(seriesData: SeriesData): Boolean {
    return this.burningSeriesQueries.seriesIsFavoriteByHref(seriesData.source).executeAsOneOrNull() ?: false
}

fun BurningSeries.setSeriesFavorite(series: Series) {
    this.burningSeriesQueries.upsertSeriesFavoriteSince(
        since = Clock.System.now().epochSeconds,
        hrefPrimary = series.source,
        href = series.href,
        season = series.season,
        seasons = series.seasons.map { it.value },
        coverHref = series.coverHref,
        fullTitle = series.title,
        mainTitle = series.mainTitle,
        subTitle = series.subTitle,
        isAnime = series.isAnime
    )
}

fun BurningSeries.unsetSeriesFavorite(series: Series) {
    this.burningSeriesQueries.upsertSeriesFavoriteSince(
        since = 0L,
        hrefPrimary = series.source,
        href = series.href,
        season = series.season,
        seasons = series.seasons.map { it.value },
        coverHref = series.coverHref,
        fullTitle = series.title,
        mainTitle = series.mainTitle,
        subTitle = series.subTitle,
        isAnime = series.isAnime
    )
}