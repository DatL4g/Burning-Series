package de.datlag.model.burningseries.allseries

import android.os.Parcelable
import androidx.room.*
import de.datlag.model.burningseries.HrefTitleBuilder
import de.datlag.model.burningseries.common.encodeToHref
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.datetime.Clock
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Obfuscate
sealed class GenreModel : HrefTitleBuilder() {
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

        @Ignore
        @Transient
        @IgnoredOnParcel
        override val href: String = String()
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
        @SerialName("href") @ColumnInfo(name = "href") override val href: String = String(),
        @ColumnInfo(name = "genreId") var genreId: Long = 0L
    ) : Parcelable, GenreModel() {
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "genreItemId")
        @IgnoredOnParcel
        var genreItemId: Long = 0L

        override fun hrefTitleFallback(): String {
            return title.encodeToHref()
        }
    }
}
