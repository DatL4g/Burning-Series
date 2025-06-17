package dev.datlag.mimasu.extension.provider.burningseries.model

data class Series(
    val href: String,
    val selectedLanguage: String?,
    val seasons: Collection<Int>,
    val languages: Collection<String>,
    val episodes: Collection<Episode>
) : SeriesData {

    override val info: SeriesData.Info = SeriesData.fromHref(href)

    val nextSeason = season?.let { current ->
        if (current <= 0) {
            return@let null
        }

        if (seasons.contains(current + 1)) {
            current + 1
        } else {
            null
        }
    }

    data class Episode(
        val number: Int?,
        val title: String,
        val href: String,
        val hoster: Collection<String>
    )
}
