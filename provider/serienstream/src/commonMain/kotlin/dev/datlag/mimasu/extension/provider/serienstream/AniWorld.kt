package dev.datlag.mimasu.extension.provider.serienstream

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem
import io.ktor.client.statement.HttpResponse

interface AniWorld {

    @GET("ajax/seriesSearch")
    suspend fun searchPlain(
        @Query("keyword", encoded = true) keyword: String
    ): Set<SearchItem.AniWorld>

    @GET("ajax/seriesSearch")
    suspend fun searchEncoded(
        @Query("keyword", encoded = false) keyword: String
    ): Set<SearchItem.AniWorld>
}