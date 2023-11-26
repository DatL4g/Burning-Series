import common.collect
import common.forEachNotNull
import common.isNullOrEmpty
import common.onReady
import dev.datlag.burningseries.model.ExtensionMessage
import kotlinx.browser.document
import kotlinx.dom.hasClass
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.Element
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.get

fun main() {
    document.onReady {
        if (document.getElementsByClassName("episodes").length <= 0) {
            checkEpisode()
        }
    }
}

private fun checkEpisode() {
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
                            entrySave(href!!, url!!) { saved ->
                                if (saved) {
                                    // apply background color
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

    val hosterArea = document.getElementsByClassName("hoster-tabs")
    hosterArea.forEachNotNull { area ->
        val hosters = area.getElementsByTagName("li")
        hosters.forEachNotNull { hoster ->
            observeActivation(hoster)
        }
    }
}

private val json = Json

private fun entrySave(href: String, url: String, callback: (Boolean) -> Unit) {
    browser.runtime.sendMessage(
        message = json.encodeToString(ExtensionMessage(
            set = true,
            href = href,
            url = url
        ))
    ).collect {
        callback((it as? Boolean) ?: false)
    }
}