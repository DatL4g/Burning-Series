package de.datlag.model.burningseries.allseries.relation

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import de.datlag.model.burningseries.allseries.GenreData
import de.datlag.model.burningseries.allseries.GenreItem
import de.datlag.model.burningseries.allseries.GenreModel
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class GenreWithItems(
    @Embedded val genre: GenreData,
    @Relation(
        entity = GenreItem::class,
        parentColumn = "genreId",
        entityColumn = "genreId"
    ) val items: List<GenreItem>
) : Parcelable {
    fun toGenreModel() = mutableListOf<GenreModel>(genre).apply { addAll(items) }
}
