package dev.datlag.mimasu.extension.provider.serienstream

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchResult

interface SerienStream {

    @GET("ajax/seriesSearch")
    suspend fun search(
        @Query("keyword") keyword: String
    ): Set<SearchResult>

    companion object {

    }
}