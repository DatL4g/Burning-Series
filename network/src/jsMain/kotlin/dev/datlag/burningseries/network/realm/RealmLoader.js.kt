package dev.datlag.burningseries.network.realm

actual object RealmLoader {
    actual suspend fun login() { }

    actual suspend fun loadEpisodes(hosterHref: List<String>): List<String> {
        return emptyList()
    }

}