package dev.datlag.mimasu.extension.provider.serienstream

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.Url
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem
import io.ktor.client.statement.HttpResponse

interface AniWorld {

    @GET
    suspend fun searchPlain(
        @Url url: String,
        @Query("keyword", encoded = true) keyword: String
    ): Set<SearchItem.AniWorld>

    @GET
    suspend fun searchEncoded(
        @Url url: String,
        @Query("keyword", encoded = false) keyword: String
    ): Set<SearchItem.AniWorld>
}