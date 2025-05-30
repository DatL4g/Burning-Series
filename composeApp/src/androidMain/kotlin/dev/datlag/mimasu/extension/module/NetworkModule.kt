package dev.datlag.mimasu.extension.module

import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.github.GitHub
import dev.datlag.mimasu.extension.github.createGitHub
import dev.datlag.mimasu.extension.provider.SearchManager
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

data object NetworkModule {

    const val NAME = "NetworkModule"

    val di = DI.Module(NAME) {
        import(PlatformModule.di)

        bindSingleton<Json> {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
        bindSingleton<GitHub> {
            val ktorfit = ktorfit {
                httpClient(instance<HttpClient>())
                baseUrl("https://api.github.com/")
            }
            ktorfit.createGitHub()
        }
        bindSingleton<SearchManager> {
            SearchManager(
                httpClient = instance(),
                fallbackClient = null
            )
        }
    }
}