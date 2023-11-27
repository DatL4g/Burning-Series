import common.collect
import common.forEachNotNull
import common.isNullOrEmpty
import common.onReady
import dev.datlag.burningseries.color.createTheme
import dev.datlag.burningseries.color.scheme.Scheme
import dev.datlag.burningseries.color.theme.Theme
import dev.datlag.burningseries.model.ExtensionMessage
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.dom.hasClass
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.*

val themes = mutableMapOf<String, Theme?>()

@OptIn(ExperimentalStdlibApi::class)
fun main() {
    document.onReady {
        val href = document.URL.ifBlank { document.location?.href } ?: document.URL
        val coverImage = document.getElementsByClassName("serie")[0]?.getElementsByTagName("img")?.get(0) as? HTMLImageElement
        if (coverImage != null) {
            if (!themes.containsKey(href)) {
                GlobalScope.launch(Dispatchers.Default) {
                    themes[href] = coverImage.createTheme()
                }
            }
        }

        if (document.getElementsByClassName("episodes").length <= 0) {
            checkEpisode(href)
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun checkEpisode(docHref: String) {
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
                                    val isDarkMode = window.matchMedia("(prefers-color-scheme: dark)").matches
                                    val (fallbackBackground, fallbackContent) = if (isDarkMode) {
                                        0xFF00497f.toInt() to 0xFFd2e4ff.toInt()
                                    } else {
                                        0xFFd2e4ff.toInt() to 0xFF001c37.toInt()
                                    }
                                    val scheme = themes[docHref]?.getScheme() ?: themes[href]?.getScheme()
                                    val (background, content) = run {
                                        (scheme?.primaryContainer ?: fallbackBackground) to (scheme?.onPrimaryContainer ?: fallbackContent)
                                    }


                                    hoster.setAttribute("style", "background-color: ${background.asHexColor()}; color: ${content.asHexColor()}")
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

private fun Theme.getScheme(): Scheme {
    val isDarkMode = window.matchMedia("(prefers-color-scheme: dark)").matches
    return if (isDarkMode) {
        this.schemes.dark
    } else {
        this.schemes.light
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun Int.asHexColor(): String {
    var hexColor = this.toHexString()
    hexColor = if (hexColor.length == 8) {
        hexColor.drop(2)
    } else {
        hexColor
    }
    return "#$hexColor"
}