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

    val searchForSeries: String

    val nextGenre: String
    val previousGenre: String

    fun openInBrowser(url: String): Boolean
}
