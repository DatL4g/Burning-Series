import common.isNullOrEmpty
import dev.datlag.burningseries.model.ExtensionMessage
import dev.datlag.burningseries.model.common.scopeCatching
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.serialization.json.Json

fun main() {
    val defaultJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    browser.runtime.onMessage.addListener {
        val msg = if (it.message.isNullOrEmpty()) {
            it
        } else {
            it.message
        }?.toString()

        if (msg.isNullOrEmpty()) {
            return@addListener false
        }

        val message: ExtensionMessage? = scopeCatching {
            defaultJson.decodeFromString<ExtensionMessage>(msg!!)
        }.getOrNull() ?: scopeCatching {
            it.unsafeCast<ExtensionMessage?>()
        }.getOrNull()

        if (message.isNullOrEmpty()) {
            return@addListener false
        }

        return@addListener GlobalScope.promise {
            if (message!!.set) {
                return@promise save(message)
            } else {
                return@promise false
            }
        }
    }
}

private suspend fun save(message: ExtensionMessage): Boolean {
    return true
}