package dev.datlag.mimasu.extension.provider.burningseries.model

data class Series(
    val selectedLanguage: String?,
    val languages: Collection<String>,
    val episodes: Collection<Episode>
) {
    data class Episode(
        val number: Int?,
        val title: String,
        val href: String,
        val hoster: Collection<String>
    )
}
