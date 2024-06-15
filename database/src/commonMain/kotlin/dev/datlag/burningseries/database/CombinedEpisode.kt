package dev.datlag.burningseries.database

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData

data class CombinedEpisode @JvmOverloads constructor(
    val default: Series.Episode,
    val database: Episode? = null
) : SeriesData() {
    override val href
        get() = default.href

    override val title: String
        get() = default.title

    override val coverHref: String?
        get() = default.coverHref

    override val mainTitle: String
        get() = default.mainTitle

    override val subTitle: String?
        get() = default.subTitle

    override val season: Int?
        get() = default.season

    override val language: String?
        get() = default.language

    val progress: Long
        get() = database?.progress ?: 0

    val length: Long
        get() = database?.length ?: 0

    val isFinished: Boolean
        get() = database?.finished ?: false

    val isWatching: Boolean
        get() = database?.watching ?: false

    val number: Int
        get() = default.convertedNumber ?: database?.number ?: -1

    val blurHash: String?
        get() = database?.blurHash?.ifBlank { null }

    fun isSame(other: Series.Episode?): Boolean {
        return default == other
    }
}
