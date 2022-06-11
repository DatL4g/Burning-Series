package de.datlag.model.burningseries.home.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import de.datlag.model.burningseries.Cover
import de.datlag.model.burningseries.home.LatestSeries

data class LatestSeriesWithCover(
    @Embedded val latestSeries: LatestSeries,
    @Relation(
        parentColumn = "latestSeriesId",
        entityColumn = "coverId",
        associateBy = Junction(LatestSeriesCoverCrossRef::class)
    ) val cover: Cover? = Cover(),
)
