package de.datlag.model.burningseries.home

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(
    tableName = "LatestEpisodeInfoFlagsTable",
    indices = [
        Index("latestEpisodeInfoFlagsId"),
        Index("classNames", unique = true)
    ]
)
@Obfuscate
data class LatestEpisodeInfoFlags(
    @ColumnInfo(name = "classNames") @SerialName("class") val classNames: String = String(),
    @ColumnInfo(name = "title") @SerialName("title") val title: String = String()
): Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "latestEpisodeInfoFlagsId")
    @IgnoredOnParcel
    var latestEpisodeInfoFlagsId: Long = 0L

    val isGerman: Boolean
        get() = classNames.split("\\s".toRegex()).any {
            it.matches("flag-de(\\s|\$)".toRegex())
        }

    val isEnglish: Boolean
        get() = classNames.split("\\s".toRegex()).any {
            it.matches("flag-en(\\s|\$)".toRegex())
        }

    val isJapanese: Boolean
        get() = classNames.split("\\s".toRegex()).any {
            it.matches("flag-jp(\\s|\$)".toRegex())
        }

    val isGermanSub: Boolean
        get() = classNames.split("\\s".toRegex()).any {
            it.matches("flag-des(\\s|\$)".toRegex())
        }

    val isEnglishSub: Boolean
        get() = classNames.split("\\s".toRegex()).any {
            it.matches("flag-ens(\\s|\$)".toRegex())
        }

    val isJapaneseSub: Boolean
        get() = classNames.split("\\s".toRegex()).any {
            it.matches("flag-jps(\\s|\$)".toRegex())
        }
}
