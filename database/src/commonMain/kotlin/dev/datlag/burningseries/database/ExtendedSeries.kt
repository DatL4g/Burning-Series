package dev.datlag.burningseries.database

import dev.datlag.burningseries.model.SeriesData

data class ExtendedSeries(
    val database: Series
) : SeriesData() {
    override val href: String
        get() = database.href

    override val title: String
        get() = database.fullTitle

    override val coverHref: String?
        get() = database.coverHref
}
