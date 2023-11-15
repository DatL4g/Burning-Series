package dev.datlag.burningseries.ui.screen.initial.favorite

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.database.Series as DBSeries

@Parcelize
sealed class FavoriteConfig : Parcelable {
    @Parcelize
    data class Series(
        val title: String,
        val href: String,
        val coverHref: String?
    ) : FavoriteConfig(), Parcelable {
        constructor(item: DBSeries) : this(item.title, item.href, item.coverHref)
    }
}