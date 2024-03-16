package dev.datlag.burningseries.shared.ui.screen.initial.home.search

import dev.datlag.burningseries.model.Genre
import kotlinx.serialization.Serializable

@Serializable
sealed class SearchConfig {
    @Serializable
    data class Series(
        val title: String,
        val href: String,
    ) : SearchConfig() {
        constructor(item: Genre.Item) : this(item.title, item.href)
    }
}