package dev.datlag.burningseries.network.realm

actual object RealmLoader {
    actual suspend fun login() { }

    actual suspend fun loadEpisodes(hosterHref: List<String>): List<String> {
        return emptyList()
    }

    actual suspend fun saveEpisode(href: String, url: String): Boolean {
        return false
    }

}