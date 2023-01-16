package dev.datlag.burningseries.other

expect class StringRes {

    val appName: String
    val allSeriesHeader: String

    val login: String

    val episodes: String
    val seriesSingular: String
    val seriesPlural: String

    val githubRepository: String

    val more: String

    val settings: String

    val showPassword: String
    val hidePassword: String

    val back: String
    val close: String
    val clear: String
    val search: String
    val confirm: String

    val searchForSeries: String

    val nextGenre: String
    val previousGenre: String

    val rewind10: String
    val play: String
    val pause: String
    val loading: String
    val forward10: String
    val fullscreen: String

    val selectLanguage: String
    val noStreamingSourceHeader: String
    val noStreamingSourceText: String
    val activate: String
    val selectSeason: String

    val saveStreamSuccess: String
    val saveStreamError: String

    val favorites: String
    val lastWatched: String
    val latestEpisodes: String
    val latestSeries: String
    val linkedSeries: String

    val about: String

    val beta: String
    val betaText: String
    val underConstruction: String
    val underConstructionText: String

    val enterUserName: String
    val enterPassword: String
    val skip: String

    val continueEpisode: String
    val startEpisode: String

    val readMore: String
    val sortHosterHint: String

    val moveUp: String
    val moveDown: String

    val hosterOrder: String
    val hosterOrderText: String
    val noHoster: String
    val noHosterText: String

    val copyright: String

    val vlcMustBeInstalled: String

    val mostPreferred: String
    val leastPreferred: String

    val tooManyRequests: String
    val errorTryAgain: String

    val loadingHome: String

    val newRelease: String
    val view: String
    val watch: String
    val `continue`: String

    val saveSuccessHeader: String
    val saveErrorHeader: String
    val saveSuccess: String
    val saveError: String

    val downloadNow: String

    val loadingAll: String
    val loadingUrl: String

    val activateText: String
    val browser: String

    fun openInBrowser(url: String): Boolean
}
