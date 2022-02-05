package de.datlag.model.burningseries.allseries

import android.os.Parcelable
import androidx.room.*
import de.datlag.model.burningseries.common.encodeToHref
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.datetime.Clock
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Obfuscate
sealed class GenreModel {
    @Parcelize
    @Serializable
    @Entity(
        tableName = "GenreTable",
        indices = [
            Index("genreId"),
            Index("genre", unique = true)
        ]
    )
    data class GenreData(
        @SerialName("genre") @ColumnInfo(name = "genre") val genre: String = String(),
        @ColumnInfo(name = "updatedAt") var updatedAt: Long = Clock.System.now().epochSeconds,
        @Ignore @SerialName("items") val items: List<GenreItem>
    ) : Parcelable, GenreModel() {
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "genreId")
        @IgnoredOnParcel
        var genreId: Long = 0L

        constructor(
            genre: String = String(),
            updatedAt: Long = Clock.System.now().epochSeconds
        ) : this(genre, updatedAt, listOf())
    }

    @Parcelize
    @Serializable
    @Entity(
        tableName = "GenreItemTable",
        indices = [
            Index("genreItemId"),
            Index("genreId"),
            Index("href", unique = true)
        ],
        foreignKeys = [
            ForeignKey(
                entity = GenreData::class,
                parentColumns = ["genreId"],
                childColumns = ["genreId"],
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
            )
        ]
    )
    data class GenreItem(
        @SerialName("title") @ColumnInfo(name = "title") val title: String = String(),
        @SerialName("href") @ColumnInfo(name = "href") val href: String = String(),
        @ColumnInfo(name = "genreId") var genreId: Long = 0L
    ) : Parcelable, GenreModel() {
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "genreItemId")
        @IgnoredOnParcel
        var genreItemId: Long = 0L

        fun getHrefTitle(): String {
            val normHref = if (href.startsWith("/")) {
                href.substring(1)
            } else { href }
            val match = Regex("(/(\\w|-)+)").find(normHref)
            return match?.groupValues?.getOrNull(1)?.replace("/", "") ?: title.encodeToHref()
        }
    }
}
