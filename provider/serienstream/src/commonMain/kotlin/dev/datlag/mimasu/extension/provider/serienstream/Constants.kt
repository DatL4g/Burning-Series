package dev.datlag.mimasu.extension.provider.serienstream

internal data object Constants {

    const val PROTOCOL_HTTPS = "https://"

    data object SerienStream {
        const val BASE_URL = "${PROTOCOL_HTTPS}s.to/"
        const val ALTERNATIVE_BASE_URL = "${PROTOCOL_HTTPS}serienstream.to/"

        const val SERIES_PATH = "serie/stream/"
        const val SEASON_PATH = "staffel-"
        const val EPISODE_PATH = "episode-"
        const val MOVIE_PATH = "film-"
    }

    data object AniWorld {
        const val BASE_URL = "${PROTOCOL_HTTPS}aniworld.to/"

        const val ANIME_PATH = "anime/stream/"
        const val SEASON_PATH = "staffel-"
        const val EPISODE_PATH = "episode-"
        const val MOVIE_PATH = "film-"
    }
}