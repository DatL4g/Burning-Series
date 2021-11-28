package de.datlag.model.burningseries.series.relation

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import de.datlag.model.burningseries.series.*
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class SeriesWithInfo(
    @Embedded val series: SeriesData,
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
    ) val episodes: List<EpisodeWithHoster>
) : Parcelable
