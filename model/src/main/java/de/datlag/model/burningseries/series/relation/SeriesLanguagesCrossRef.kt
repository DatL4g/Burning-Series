package de.datlag.model.burningseries.series.relation

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.datlag.model.burningseries.series.LanguageData
import de.datlag.model.burningseries.series.SeriesData
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(
    tableName = "SeriesLanguagesCrossTable",
    primaryKeys = ["seriesId", "languageId"],
    indices = [
        Index("seriesId"),
        Index("languageId")
    ]
)
@Obfuscate
data class SeriesLanguagesCrossRef(
    val seriesId: Long,
    val languageId: Long
) : Parcelable
