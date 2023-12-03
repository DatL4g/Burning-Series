package dev.datlag.burningseries.network

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.Path
import dev.datlag.burningseries.model.Release

interface GitHub {

    @Headers("Accept: application/vnd.github+json")
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<Release>
}