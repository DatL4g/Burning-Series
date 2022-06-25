package de.datlag.model.burningseries.series.relation

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import de.datlag.model.burningseries.Cover
import de.datlag.model.burningseries.series.*
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.math.max

@Parcelize
@Serializable
@Obfuscate
data class SeriesWithInfo(
    @Embedded val series: SeriesData,
    @Relation(
        parentColumn = "seriesId",
        entityColumn = "coverId",
        associateBy = Junction(SeriesCoverCrossRef::class)
    ) val cover: Cover? = Cover(),
    @Relation(
        entity = InfoData::class,
        parentColumn = "seriesId",
        entityColumn = "seriesId"
    ) val infos: List<InfoData>,
    @Relation(
        entity = SeasonData::class,
        parentColumn = "seriesId",
        entityColumn = "seriesId"
    ) val seasons: List<SeasonData>,
    @Relation(
        parentColumn = "seriesId",
        entityColumn = "languageId",
        associateBy = Junction(SeriesLanguagesCrossRef::class)
    ) val languages: List<LanguageData>,
    @Relation(
        entity = EpisodeInfo::class,
        parentColumn = "seriesId",
        entityColumn = "seriesId"
    ) val episodes: List<EpisodeWithHoster>,
    @Relation(
        entity = LinkedSeriesData::class,
        parentColumn = "seriesId",
        entityColumn = "seriesId"
    ) val linkedSeries: List<LinkedSeriesData>
) : Parcelable {

    private val currentSeasonMaxValue: Int
        get() = if (seasons.isNotEmpty() && series.seasons.isNotEmpty()) {
            max(series.currentSeason(seasons)?.value ?: Int.MIN_VALUE, series.currentSeason(series.seasons)?.value ?: Int.MIN_VALUE)
        } else if (seasons.isNotEmpty()) {
            series.currentSeason(seasons)?.value ?: Int.MIN_VALUE
        } else if (series.seasons.isNotEmpty()) {
            series.currentSeason(series.seasons)?.value ?: Int.MIN_VALUE
        } else {
            Int.MIN_VALUE
        }

    val currentSeasonIsFirst: Boolean
        get() = currentSeasonMaxValue == 1

    val currentSeasonIsLast: Boolean
        get() {
            val seasonsMax = seasons.maxOfOrNull { it.value } ?: series.seasons.maxOfOrNull { it.value } ?: Int.MAX_VALUE
            return currentSeasonMaxValue >= seasonsMax
        }
}
