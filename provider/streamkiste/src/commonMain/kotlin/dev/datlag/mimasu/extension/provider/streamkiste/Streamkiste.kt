package dev.datlag.mimasu.extension.provider.streamkiste

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import dev.datlag.mimasu.extension.provider.streamkiste.model.Browse
import dev.datlag.mimasu.extension.provider.streamkiste.model.Season

interface Streamkiste {
    
    @GET("data/browse")
    suspend fun browsePlain(
        @Query("lang") lang: Int,
        @Query("keyword", encoded = true) keyword: String,
        @Query("year") year: Int?,
        @Query("type") type: String?
    ): Browse

    @GET("data/browse")
    suspend fun browseEncoded(
        @Query("lang") lang: Int,
        @Query("keyword", encoded = false) keyword: String,
        @Query("year") year: Int?,
        @Query("type") type: String?
    ): Browse

    @GET("data/seasons")
    suspend fun seasons(
        @Query("lang") lang: Int,
        @Query("original_title") originalTitle: String
    ): Set<Season>

    companion object {
        internal const val LANG_EN = 1
        internal const val LANG_DE = 2

        const val BASE_URL = "https://streamkiste.sx/"
    }
}