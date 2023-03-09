import common.collect
import common.forEachNotNull
import common.isNullOrEmpty
import common.onReady
import dev.datlag.burningseries.model.ExtensionMessage
import kotlinx.browser.document
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.Element
import org.w3c.dom.get

fun main() {
    document.onReady {
        if (document.getElementsByClassName("episodes").isNullOrEmpty()) {
            // check episode
        } else {
            checkSeason()
        }
    }
}

private fun checkSeason() {
    fun checkHoster(hoster: Element) {
        hoster.getAttribute("href")?.let { href ->
            dbEntryExists(href) { exists ->
                if (exists) {
                    hoster.setAttribute("style", "background-color: #f5cd67;")
                }
            }
        }
    }

    val episodes = document.getElementsByClassName("episodes")[0]?.getElementsByTagName("tr")
    episodes.forEachNotNull { episode ->
        val hosters = episode.getElementsByTagName("td")[2]?.getElementsByTagName("a")
        hosters.forEachNotNull { hoster ->
            checkHoster(hoster)
        }
    }
}

private fun dbEntryExists(href: String, callback: (Boolean) -> Unit) {
    val json = Json
    browser.runtime.sendMessage(
        message = json.encodeToString(ExtensionMessage(
            query = ExtensionMessage.QueryType.EXISTS,
            id = href
        ))
    )?.collect {
        callback(it as? Boolean ?: false)
    }
}

