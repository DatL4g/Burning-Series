package de.datlag.model.burningseries.series.relation

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import de.datlag.model.burningseries.series.EpisodeInfo
import de.datlag.model.burningseries.series.HosterData
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class EpisodeWithHoster(
    @Embedded val episode: EpisodeInfo,
    @Relation(
        entity = HosterData::class,
        parentColumn = "episodeId",
        entityColumn = "episodeId"
    ) val hoster: List<HosterData> = listOf()
) : Parcelable
