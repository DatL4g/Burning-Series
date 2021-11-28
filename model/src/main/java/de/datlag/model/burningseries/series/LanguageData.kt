package de.datlag.model.burningseries.series

import android.os.Parcelable
import androidx.room.*
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(
    tableName = "LanguageTable",
    indices = [
        Index("languageId"),
        Index("value", unique = true)
    ]
)
@Obfuscate
data class LanguageData(
    @ColumnInfo(name = "value") @SerialName("value") val value: String = String(),
    @ColumnInfo(name = "text") @SerialName("text") val text: String = String()
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "languageId")
    @IgnoredOnParcel
    var languageId: Long = 0L
}
