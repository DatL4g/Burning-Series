package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.Cover

expect object CoverScraper {

    suspend fun getCover(cover: String?, isNsfw: Boolean): Pair<Cover, Boolean>
}