package dev.datlag.burningseries.shared.ui.screen.initial.favorite

import kotlinx.serialization.Serializable
import dev.datlag.burningseries.database.Series as DBSeries

@Serializable
sealed class FavoriteConfig {
    @Serializable
    data class Series(
        val title: String,
        val href: String,
        val coverHref: String?
    ) : FavoriteConfig() {
        constructor(item: DBSeries) : this(item.title, item.href, item.coverHref)
    }
}