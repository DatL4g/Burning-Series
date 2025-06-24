package dev.datlag.mimasu.extension.provider.serienstream.model

import kotlinx.serialization.Serializable

interface SeriesData {

    abstract val info: Info

    val source: String
        get() = info.source

    val season: Int?
        get() = info.season

    @Serializable
    data class Info(
        val source: String,
        val season: Int?
    ) {

        fun toSlug(newSeason: Int? = season): String {
            val season = newSeason?.let { s ->
                if (s <= 0) {
                    "filme"
                } else {
                    "staffel-$newSeason"
                }
            }

            return if (season.isNullOrBlank()) {
                source
            } else {
                "$source/$season"
            }
        }
    }

    companion object {
        fun fromSearchItem(searchItem: SearchItem, startInfo: String = searchItem.slugInfoStart()): Info {
            val slugSplit = startInfo.split('/')
            return Info(
                source = slugSplit[0],
                season = searchItem.seasonFrom(slugSplit.getOrElse(1, defaultValue = { startInfo }))
            )
        }
    }
}