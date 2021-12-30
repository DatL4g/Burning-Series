package de.datlag.model.burningseries.allseries.search

import androidx.room.ColumnInfo
import androidx.room.Embedded
import de.datlag.model.burningseries.allseries.GenreModel
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
data class GenreItemWithMatchInfo(
    @Embedded val genreItem: GenreModel.GenreItem,
    @ColumnInfo(name = "matchInfo") val matchInfo: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenreItemWithMatchInfo

        if (genreItem != other.genreItem) return false
        if (!matchInfo.contentEquals(other.matchInfo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = genreItem.hashCode()
        result = 31 * result + matchInfo.contentHashCode()
        return result
    }
}
