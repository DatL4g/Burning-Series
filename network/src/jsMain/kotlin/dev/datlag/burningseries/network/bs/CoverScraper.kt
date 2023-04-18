package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.Cover

actual object CoverScraper {
    actual suspend fun getCover(
        cover: String?,
        isNsfw: Boolean
    ): Pair<Cover, Boolean> {
        return Cover(String()) to false
    }
}