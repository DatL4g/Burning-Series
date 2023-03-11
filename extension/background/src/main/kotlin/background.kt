import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import common.isNullOrEmpty
import de.jensklingenberg.ktorfit.ktorfit
import de.jensklingenberg.ktorfit.ktorfitBuilder
import dev.datlag.burningseries.model.ExtensionMessage
import dev.datlag.burningseries.model.ScrapedHoster
import dev.datlag.burningseries.model.algorithm.MD5
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.converter.FlowerResponseConverter
import dev.datlag.burningseries.network.repository.SaveRepository
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun main() {
    val defaultJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val ktorfit = ktorfitBuilder {
        responseConverter(FlowerResponseConverter())
        httpClient(Js) {
            developmentMode = false
            followRedirects = true
            install(ContentNegotiation) {
                json(defaultJson)
            }
        }
    }
    val jsonBaseKtor = ktorfit.build {
        baseUrl("https://jsonbase.com/")
    }
    val burningSeriesKtor = ktorfit.build {
        baseUrl("https://api.datlag.dev/bs/")
    }
    val jsonBase = jsonBaseKtor.create<JsonBase>()
    val burningSeries = burningSeriesKtor.create<BurningSeries>()
    val saveRepo = SaveRepository(burningSeries, jsonBase, null)

    browser.runtime.onMessage.addListener {
        val msg = if (it.message.isNullOrEmpty()) {
            it
        } else {
            it.message
        }?.toString()

        if (msg.isNullOrEmpty()) {
            return@addListener false
        }

        val message: ExtensionMessage? = try {
            defaultJson.decodeFromString(msg!!)
        } catch (ignored: Throwable) {
            try {
                it.unsafeCast<ExtensionMessage?>()
            } catch (ignored: Throwable) {
                null
            }
        }

        if (message.isNullOrEmpty()) {
            return@addListener false
        }

        return@addListener GlobalScope.promise {
            when (message!!.query) {
                ExtensionMessage.QueryType.EXISTS -> {
                    return@promise exists(jsonBase, message)
                }
                ExtensionMessage.QueryType.SET -> {
                    return@promise save(saveRepo, message)
                }
                ExtensionMessage.QueryType.GET -> {
                    return@promise get(jsonBase, message)
                }

                else -> {
                    false
                }
            }
        }
    }
}

private suspend fun exists(jsonBase: JsonBase, message: ExtensionMessage): Boolean {
    val id = MD5.hexString(message.id)
    val result = networkResource(makeNetworkRequest = {
        jsonBase.burningSeriesCaptcha(id)
    }).mapNotNull {
        when (it.status) {
            is Resource.Status.Loading -> null
            is Resource.Status.EmptySuccess -> Resource.error(String(), 0, null)
            else -> it
        }
    }.first()

    return result.status is Resource.Status.Success && !(result.status as Resource.Status.Success).data.broken
}

private suspend fun save(repository: SaveRepository, message: ExtensionMessage): Boolean {
    return repository.save(ScrapedHoster(
        href = message.id,
        url = message.url!!
    ))
}

private suspend fun get(jsonBase: JsonBase, message: ExtensionMessage): String? {
    val id = MD5.hexString(message.id)
    return networkResource(makeNetworkRequest = {
        jsonBase.burningSeriesCaptcha(id)
    }).mapNotNull {
        when (it.status) {
            is Resource.Status.Loading -> null
            else -> it
        }
    }.map {
        when (it.status) {
            is Resource.Status.Success -> {
                val data = (it.status as Resource.Status.Success).data
                if (data.broken) {
                    null
                } else {
                    data.url
                }
            }
            else -> null
        }
    }.first()
}