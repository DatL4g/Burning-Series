package dev.datlag.mimasu.extension.github

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import dev.datlag.mimasu.extension.github.model.Release

interface GitHub {

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun latestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Release
}