package dev.datlag.mimasu.extension.provider.serienstream

internal data object Constants {

    const val PROTOCOL_HTTPS = "https://"

    data object SerienStream {
        val DOMAINS = setOf(
            "s.to",
            "serienstream.to",
            "serienstream.cx",
        )

        const val SERIES_PATH = "serie/stream/"
        const val SEASON_PATH = "staffel-"
        const val EPISODE_PATH = "episode-"
        const val MOVIE_PATH = "film-"
    }

    data object AniWorld {
        val DOMAINS = setOf(
            "aniworld.to",
            "aniworld.cc"
        )

        const val ANIME_PATH = "anime/stream/"
        const val SEASON_PATH = "staffel-"
        const val EPISODE_PATH = "episode-"
        const val MOVIE_PATH = "film-"
    }
}