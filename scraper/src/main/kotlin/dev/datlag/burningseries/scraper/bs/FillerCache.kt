package dev.datlag.burningseries.scraper.bs

import dev.datlag.burningseries.model.Series

object FillerCache {

    private var showList: Series.Shows? = null
    private val nameToSlugRegex = "([A-Za-z 0-9]+)".toRegex()

    fun loadShows(): Boolean {
        return showList?.data.isNullOrEmpty()
    }

    fun addAllShows(shows: Series.Shows) {
        showList = shows
    }

    fun getAllShows(): List<Series.Shows.Show> {
        return showList?.data ?: emptyList()
    }

    fun findShow(name: String): Series.Shows.Show? {
        return getAllShows().find { it.name.equals(name, true) }
            ?: getAllShows().find { it.slug.equals(name, true) }
            ?: getAllShows().find { it.slug.equals(name.replace(' ', '-'), true) }
            ?: run {
                val nameAsSlug = nameToSlugRegex.findAll(name).toList().mapNotNull {
                    it.value.trim().ifEmpty { null }
                }.joinToString(separator = "-", truncated = " ").trim().replace(' ', '-')
                getAllShows().find { it.slug.equals(nameAsSlug, true) }
            }
    }
}