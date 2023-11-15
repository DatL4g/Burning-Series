package dev.datlag.burningseries.ui.screen.initial.search

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Genre

@Parcelize
sealed class SearchConfig : Parcelable {
    @Parcelize
    data class Series(
        val title: String,
        val href: String,
    ) : SearchConfig(), Parcelable {
        constructor(item: Genre.Item) : this(item.title, item.href)
    }
}