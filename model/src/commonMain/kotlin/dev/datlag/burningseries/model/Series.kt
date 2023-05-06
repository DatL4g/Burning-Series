package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.IgnoredOnParcel
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.common.getDigitsOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Parcelize
@Serializable
data class Series(
    @SerialName("title") val title: String,
    @SerialName("season") val season: String,
    @SerialName("description") val description: String,
    @SerialName("selectedLanguage") val selectedLanguage: String,
    @SerialName("cover") val cover: Cover,
    @SerialName("isNsfw") val isNsfw: Boolean,
    @SerialName("infos") val infos: List<Info>,
    @SerialName("languages") val languages: List<Language>,
    @SerialName("seasons") val seasons: List<Season>,
    @SerialName("episodes") val episodes: List<Episode>,
    @SerialName("linkedSeries") val linkedSeries: List<Linked>,
    @SerialName("href") val href: String
) : Parcelable {

    fun currentSeason(seasons: List<Season> = this.seasons): Season? {
        return seasons.find {
            it.title.equals(season, true)
                    || it.title.trim().equals(season.trim(), true)
                    || it.title.equals(season.toIntOrNull()?.toString(), true)
                    || it.title.toIntOrNull()?.toString()?.equals(season, true) == true
                    || it.title.toIntOrNull()?.toString()?.equals(season.toIntOrNull()?.toString(), true) == true
                    || it.title.equals(season.getDigitsOrNull(), true)
                    || it.title.getDigitsOrNull()?.equals(season, true) == true
                    || it.title.getDigitsOrNull()?.equals(season.getDigitsOrNull(), true) == true
        }
    }

    fun hrefBuilder(season: Int?, language: String = selectedLanguage): String {
        val hrefData = BSUtil.hrefDataFromHref(
            BSUtil.normalizeHref(href)
        )

        return BSUtil.fixSeriesHref(
            BSUtil.rebuildHrefFromData(
                Triple(
                    first = hrefData.first,
                    second = season?.toString() ?: hrefData.second,
                    third = language
                )
            )
        )
    }

    @Parcelize
    @Serializable
    data class Info(
        @SerialName("header") val header: String,
        @SerialName("data") val data: String
    ): Parcelable {
        fun isGenre(): Boolean {
            return this.header.trim().equals("Genre", true) || this.header.trim().equals("Genres", true)
        }

        fun trimmedData(): String {
            var newData = data.trim()
            if (newData.contains(',')) {
                newData = newData.split(',').joinToString { it.trim() }
            }
            return newData
        }
    }

    @Parcelize
    @Serializable
    data class Language(
        @SerialName("value") val value: String,
        @SerialName("text") val text: String
    ) : Parcelable

    @Parcelize
    @Serializable
    data class Season(
        @SerialName("title") val title: String,
        @SerialName("value") val value: Int
    ) : Parcelable

    @Parcelize
    @Serializable
    data class Episode(
        @SerialName("number") val number: String,
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("watched") val watched: Boolean? = null,
        @SerialName("watchHref") val watchHref: String? = null,
        @SerialName("isCanon") val isCanon: Boolean? = null,
        @SerialName("isFiller") val isFiller: Boolean? = null,
        @SerialName("hoster") val hoster: List<Hoster>
    ) : Parcelable {

        @Transient
        @IgnoredOnParcel
        var length: Long = 0L

        @Transient
        @IgnoredOnParcel
        var watchPosition: Long = 0L

        @IgnoredOnParcel
        val isFinished: Boolean
            get() = watchPercentage() > 85F

        @IgnoredOnParcel
        val episodeNumber: Int?
            get() {
                val matched = "[|({]\\s*Ep([.]|isode)?\\s*(\\d+)\\s*[|)}]".toRegex(RegexOption.IGNORE_CASE).find(title.trim())
                return matched?.groupValues?.let {
                    val numberMatch = it.lastOrNull()
                    numberMatch?.toIntOrNull() ?: numberMatch?.getDigitsOrNull()?.toIntOrNull()
                }
            }

        @IgnoredOnParcel
        val episodeNumberOrListNumber: Int?
            get() = episodeNumber ?: number.toIntOrNull() ?: number.getDigitsOrNull()?.toIntOrNull()

        fun watchPercentage(): Float {
            if (watchPosition == 0L || length == 0L) {
                return 0F
            } else if (length in 1 until watchPosition) {
                return 100F
            }
            return ((watchPosition.toDouble() * 100) / length.toDouble()).toFloat()
        }

        fun getWatchState(): WatchState = WatchState.getByPercentage(watchPercentage())

        @Parcelize
        sealed interface WatchState : Parcelable {
            object NONE : WatchState
            object STARTED : WatchState
            object FINISHED : WatchState

            companion object {
                fun getByPercentage(percentage: Float): WatchState = when {
                    percentage > 85F -> WatchState.FINISHED
                    percentage > 0F -> WatchState.STARTED
                    else -> WatchState.NONE
                }
            }
        }

        @Parcelize
        @Serializable
        data class Hoster(
            @SerialName("title") val title: String,
            @SerialName("href") val href: String
        ) : Parcelable
    }

    @Parcelize
    @Serializable
    data class Linked(
        @SerialName("isSpinOff") val isSpinOff: Boolean,
        @SerialName("isMainStory") val isMainStory: Boolean,
        @SerialName("title") val title: String,
        @SerialName("href") val href: String
    ) : Parcelable

    @Parcelize
    @Serializable
    data class Slug(
        @SerialName("data") val data: SlugData
    ) : Parcelable {

        @Parcelize
        @Serializable
        data class SlugData(
            @SerialName("cannonEpisodes") val cannonEpisodes: List<Int> = emptyList(),
            @SerialName("fillerEpisodes") val fillerEpisodes: List<Int> = emptyList(),
            @SerialName("mixedEpisodes") val mixedEpisodes: List<Int> = emptyList(),
        ) : Parcelable
    }

    @Parcelize
    @Serializable
    data class Shows(
        @SerialName("data") val data: List<Show> = emptyList()
    ): Parcelable {

        @Parcelize
        @Serializable
        data class Show(
            @SerialName("name") val name: String = String(),
            @SerialName("slug") val slug: String = String()
        ): Parcelable
    }
}
