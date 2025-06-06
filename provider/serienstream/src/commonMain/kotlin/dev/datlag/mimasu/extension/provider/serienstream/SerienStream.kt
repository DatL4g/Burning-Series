package dev.datlag.mimasu.extension.provider.serienstream

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchResult
import io.ktor.client.statement.HttpResponse

interface SerienStream {

    @GET("ajax/seriesSearch")
    suspend fun searchPlain(
        @Query("keyword", encoded = true) keyword: String
    ): Set<SearchResult>

    @GET("ajax/seriesSearch")
    suspend fun searchEncoded(
        @Query("keyword", encoded = false) keyword: String
    ): Set<SearchResult>

}