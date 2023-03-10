import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import common.isNullOrEmpty
import common.toHexString
import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.burningseries.model.ExtensionMessage
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.converter.FlowerResponseConverter
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun main() {
    val defaultJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val ktorfit = ktorfit {
        baseUrl("https://jsonbase.com/")
        responseConverter(FlowerResponseConverter())
        httpClient(Js) {
            developmentMode = false
            followRedirects = true
            install(ContentNegotiation) {
                json(defaultJson)
            }
        }
    }

    val jsonBase = ktorfit.create<JsonBase>()

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

                else -> {
                    false
                }
            }
        }
    }
}

private suspend fun exists(jsonBase: JsonBase, message: ExtensionMessage): Boolean {
    val id = MD5.compute(message.id.encodeToByteArray()).toHexString()
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