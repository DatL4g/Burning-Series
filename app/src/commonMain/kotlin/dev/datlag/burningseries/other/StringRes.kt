package dev.datlag.burningseries.other

expect class StringRes {

    val appName: String

    val login: String

    val episodes: String
    val seriesSingular: String
    val seriesPlural: String

    val githubRepository: String

    val more: String

    val settings: String

    val showPassword: String
    val hidePassword: String

    fun openInBrowser(url: String): Boolean
}
