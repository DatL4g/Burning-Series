package dev.datlag.mimasu.extension.provider.burningseries.model

import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import kotlinx.serialization.Serializable

interface SeriesData {

    abstract val info: Info

    val source: String
        get() = info.source

    val season: Int?
        get() = info.season

    val language: String?
        get() = info.language

    fun toHref(
        newSeason: Int? = season,
        newLanguage: String? = language
    ): String = info.toHref(newSeason = newSeason, newLanguage = newLanguage)

    @Serializable
    data class Info(
        val source: String,
        val season: Int?,
        val language: String?
    ) {

        fun toHref(newSeason: Int? = season, newLanguage: String? = language): String {
            return if (newSeason != null && !newLanguage.isNullOrBlank()) {
                "serie/${source}/${newSeason}/${newLanguage}"
            } else if (newSeason != null) {
                "serie/${source}/${newSeason}"
            } else if (!newLanguage.isNullOrBlank()) {
                "serie/${source}/${newLanguage}"
            } else {
                "serie/${source}"
            }
        }
    }

    companion object {

        private fun getHrefTitle(href: String): String {
            val newHref = if (href.startsWith("series/")) {
                href.substringAfter("series/")
            } else if (href.startsWith("serie/")) {
                href.substringAfter("serie/")
            } else if (href.startsWith("/series/")) {
                href.substringAfter("/series/")
            } else if (href.startsWith("/serie/")) {
                href.substringAfter("/serie/")
            } else {
                href
            }

            val potentialTitle = if (newHref.startsWith('/')) {
                newHref.substringAfter('/')
            } else {
                newHref
            }
            val title = potentialTitle.substringBefore('/').trim()
            return if (title.equals("serie", ignoreCase = true) || title.equals("series", ignoreCase = true)) {
                title.substringAfter('/').substringBefore('/').trim()
            } else {
                title
            }
        }

        fun fromHref(href: String): Info {
            var newHref = BurningSeries.normalize(href)
            if (newHref.startsWith('/')) {
                newHref = newHref.substring(1)
            }
            if (newHref.startsWith("serie/", true) || newHref.startsWith("series/", true)) {
                newHref = newHref.substringAfter('/')
            }

            val hrefSplit = newHref.split('/')
            val season = if (hrefSplit.size >= 2) hrefSplit[1] else null
            val convertedSeason = season?.ifBlank { null }?.trim()?.toIntOrNull()
            val missingSeasonLanguage = if (convertedSeason == null && hrefSplit.size == 2) {
                hrefSplit[1].trim()
            } else null
            val language = if (hrefSplit.size >= 3) {
                hrefSplit[2].trim()
            } else null
            val fallbackLanguage = if (hrefSplit.size >= 4) {
                hrefSplit[3].trim()
            } else null
            var title = hrefSplit[0].ifBlank { getHrefTitle(href) }

            if (title.equals(season, ignoreCase = true)) {
                title = getHrefTitle(href)
            }

            return Info(
                source = title,
                season = convertedSeason,
                language = if (!fallbackLanguage.isNullOrBlank()) {
                    fallbackLanguage
                } else {
                    if (language.isNullOrBlank()) missingSeasonLanguage else language
                }
            )
        }
    }
}