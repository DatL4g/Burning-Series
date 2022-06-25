package de.datlag.model.burningseries.allseries.search

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import de.datlag.model.burningseries.allseries.GenreItem
import io.michaelrocks.paranoid.Obfuscate

@Entity(
    tableName = "GenreItemFTS"
)
@Fts4(contentEntity = GenreItem::class, tokenizer = FtsOptions.TOKENIZER_PORTER)
@Obfuscate
data class GenreItemFTS(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "href") val href: String
)
