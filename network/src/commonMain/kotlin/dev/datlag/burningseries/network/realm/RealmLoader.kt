package dev.datlag.burningseries.network.realm

expect class RealmLoader {
    suspend fun login()

    suspend fun loadEpisodes(hosterHref: List<String>): List<String>
}