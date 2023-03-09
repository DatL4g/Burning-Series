import common.isNullOrEmpty
import common.toHexString
import dev.datlag.burningseries.model.ExtensionMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.js.Promise

fun main() {
    browser.runtime.onMessage.addListener {
        val msg = if (it.message.isNullOrEmpty()) {
            it
        } else {
            it.message
        }?.toString()

        if (msg.isNullOrEmpty()) {
            return@addListener Promise.resolve(false)
        }

        val message: ExtensionMessage? = try {
            Json.decodeFromString(msg!!)
        } catch (ignored: Throwable) {
            try {
                it.unsafeCast<ExtensionMessage?>()
            } catch (ignored: Throwable) {
                null
            }
        }

        if (message.isNullOrEmpty()) {
            return@addListener Promise.resolve(false)
        }

        CoroutineScope(Dispatchers.Default).promise {
            console.log(MD5.compute(message!!.id.encodeToByteArray()).toHexString())
        }

        return@addListener Promise.resolve(true)
    }
}

private suspend fun exists(message: ExtensionMessage): Boolean {
    return true
}
