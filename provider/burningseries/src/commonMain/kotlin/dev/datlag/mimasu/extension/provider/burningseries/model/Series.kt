package dev.datlag.mimasu.extension.provider.burningseries.model

data class Series(
    val episodes: Collection<Episode>
) {
    data class Episode(
        val number: Int?,
        val title: String,
        val href: String,
        val hoster: Collection<String>
    )
}
