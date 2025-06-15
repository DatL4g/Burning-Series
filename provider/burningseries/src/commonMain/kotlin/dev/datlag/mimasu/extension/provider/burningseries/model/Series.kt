package dev.datlag.mimasu.extension.provider.burningseries.model

data class Series(
    val href: String,
    val selectedLanguage: String?,
    val languages: Collection<String>,
    val episodes: Collection<Episode>
) : SeriesData {

    override val info: SeriesData.Info = SeriesData.fromHref(href)

    data class Episode(
        val number: Int?,
        val title: String,
        val href: String,
        val hoster: Collection<String>
    )
}
