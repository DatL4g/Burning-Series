import common.*
import dev.datlag.burningseries.model.ExtensionMessage
import kotlinx.browser.document
import kotlinx.dom.hasClass
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.Element
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.get
import kotlin.js.Promise

fun main() {
    document.onReady {
        if (document.getElementsByClassName("episodes").length <= 0) {
            checkEpisode()
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
                    getActivatedColor().collect { color ->
                        val styleBuilder = buildString {
                            append("background-color: $color;")
                            append("border: 3px solid $color;")
                            append("border-radius: 3px;")
                        }
                        hoster.setAttribute("style", styleBuilder)
                    }
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

private fun checkEpisode() {
    fun checkHoster(hoster: Element) {
        if (hoster.hasClass("active")) {
            return
        }
        hoster.getElementsByTagName("a")[0]?.getAttribute("href")?.let { href ->
            dbEntryExists(href) { exists ->
                if (exists) {
                    getActivatedColor().collect { color ->
                        val styleBuilder = buildString {
                            append("background-color: $color;")
                        }
                        hoster.setAttribute("style", styleBuilder)
                    }
                }
            }
        }
    }

    fun observeActivation(hoster: Element) {
        if (!hoster.hasClass("active")) {
            return
        }

        val observer = MutationObserver { mutations, _ ->
            for (mutation in mutations) {
                if (mutation.addedNodes.length <= 0) {
                    continue
                }
                for (i in 0 until mutation.addedNodes.length) {
                    val node = mutation.addedNodes[i]
                    if (!node.isNullOrEmpty() && !node.asDynamic().tagName.unsafeCast<String?>().isNullOrEmpty()) {
                        val tagName = node.asDynamic().tagName.unsafeCast<String?>()
                        val url = if (tagName!!.equals("a", true)) {
                            (node as? Element)?.getAttribute("href")
                        } else if (tagName.equals("iframe", true)) {
                            (node as? Element)?.getAttribute("src")
                        } else {
                            null
                        }
                        val href = hoster.getElementsByTagName("a")[0]?.getAttribute("href")
                        if (!url.isNullOrEmpty() && !href.isNullOrEmpty()) {
                            dbEntrySave(href!!, url!!) { saved ->
                                if (saved) {
                                    getActivatedColor().collect { color ->
                                        val styleBuilder = buildString {
                                            append("background-color: $color;")
                                        }
                                        hoster.setAttribute("style", styleBuilder)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        document.getElementsByClassName("hoster-player")[0]?.let { node ->
            observer.observe(
                node,
                MutationObserverInit(
                    childList = true,
                    subtree = true,
                    attributes = false,
                    characterData = false
                )
            )
        }
    }

    fun getSavedHoster(tabArea: Element, hoster: Element) {
        if (!hoster.hasClass("active")) {
            return
        }

        hoster.getElementsByTagName("a")[0]?.getAttribute("href")?.let { href ->
            dbEntryGet(href) { url ->
                if (url.isNullOrEmpty()) {
                    return@dbEntryGet
                }

                getActivatedColor().collect { color ->
                    val hosterStyle = buildString {
                        append("background-color: $color;")
                    }
                    hoster.setAttribute("style", hosterStyle)

                    val newDiv = document.createElement("div")
                    val styleBuilder = buildString {
                        append("border-color: $color;")
                        append("border-width: 5px;")
                        append("border-radius: 5px;")
                        append("border-style: solid;")
                        append("width: 100%;")
                        append("height: 100px;")
                        append("display: flex;")
                        append("justify-content: center;")
                        append("align-items: center;")
                        append("align-content: center;")
                    }
                    val a = document.createElement("a")
                    val linkText = document.createTextNode(url!!)
                    a.appendChild(linkText)
                    a.setAttribute("href", url)
                    a.setAttribute("title", url)
                    a.setAttribute("target", "_blank")

                    newDiv.setAttribute("style", styleBuilder)
                    newDiv.appendChild(a)

                    tabArea.insertAfter(newDiv)
                }
            }
        }
    }

    val hosterArea = document.getElementsByClassName("hoster-tabs")
    hosterArea.forEachNotNull { area ->
        val hosters = area.getElementsByTagName("li")
        hosters.forEachNotNull { hoster ->
            checkHoster(hoster)
            observeActivation(hoster)
            getSavedHoster(area, hoster)
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

private fun dbEntrySave(href: String, url: String, callback: (Boolean) -> Unit) {
    val json = Json
    browser.runtime.sendMessage(
        message = json.encodeToString(ExtensionMessage(
            query = ExtensionMessage.QueryType.SET,
            id = href,
            url = url
        ))
    ).collect {
        callback(it as? Boolean ?: false)
    }
}

private fun dbEntryGet(href: String, callback: (String?) -> Unit) {
    val json = Json
    browser.runtime.sendMessage(
        message = json.encodeToString(ExtensionMessage(
            query = ExtensionMessage.QueryType.GET,
            id = href
        ))
    ).collect {
        callback(it as? String)
    }
}

private fun getActivatedColor(): Promise<String> {
    return browser.storage.sync.get("color").then {
        val colorItem = runCatching {
            it as? String
        }.getOrNull() ?: runCatching {
            it.unsafeCast<String?>()
        }.getOrNull()
        (colorItem as? String) ?: it.asDynamic().color.unsafeCast<String?>() ?: "#f5cd67"
    }
}