package dev.datlag.mimasu.extension.provider

import io.ktor.client.HttpClient

class EpisodeManager(
    val httpClient: HttpClient,
    val fallbackClient: HttpClient?,
) {



}