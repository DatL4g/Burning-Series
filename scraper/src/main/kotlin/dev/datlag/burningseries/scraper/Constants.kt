package dev.datlag.burningseries.scraper

object Constants {

    const val PROTOCOL_HTTP = "http://"
    const val PROTOCOL_HTTPS = "https://"

    const val HOST_BS_TO = "bs.to"

    const val ANIME_FILLER_SHOWS_URL = "https://anime-fillerlist-api.vercel.app/v1/shows"
    const val ANIME_FILLER_SLUG_URL = "https://anime-fillerlist-api.vercel.app/v1/show/"

    fun getBurningSeriesLink(href: String, http: Boolean = false, host: String = HOST_BS_TO): String {
        return if (!href.matches("^\\w+?://.*".toRegex())) {
            if (!href.startsWith("/")) {
                "${if (http) PROTOCOL_HTTP else PROTOCOL_HTTPS}${host}/$href"
            } else {
                "${if (http) PROTOCOL_HTTP else PROTOCOL_HTTPS}${host}${"(?!:|/{2,})(/.*)".toRegex().find(href)?.value}"
            }
        } else {
            href
        }
    }

    val episodeNumberRegex = "[|({]\\s*Ep([.]|isode)?\\s*(\\d+)\\s*[|)}]".toRegex(RegexOption.IGNORE_CASE)

}