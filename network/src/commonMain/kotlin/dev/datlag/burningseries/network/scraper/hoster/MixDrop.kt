package dev.datlag.burningseries.network.scraper.hoster

import dev.datlag.burningseries.model.common.listFrom
import dev.datlag.jsunpacker.JsUnpacker
import ktsoup.KtSoupDocument

class MixDrop : Manipulation {
    override fun match(url: String): Boolean {
        return "(?://|\\.)(mixdro?p\\.(?:c[ho]|to|sx|bz|gl|club))/(?:f|e)/(\\w+)".toRegex().containsMatchIn(url)
    }

    override fun change(initialList: Collection<String>, doc: KtSoupDocument): List<String> {
        val mixDropResult = JsUnpacker.unpack(doc.querySelectorAll("script").map { it.html() }).flatMap {
            "wurl=\\s*\"(.*?)\"".toRegex().findAll(it).toList().mapNotNull { result ->
                val url = result.groups[1]?.value?.trim()?.ifBlank { null }
                return@mapNotNull if (url == null) {
                    null
                } else {
                    if (url.startsWith("//")) {
                        "https:$url"
                    } else {
                        url
                    }
                }
            }
        }
        return listFrom(
            initialList,
            mixDropResult
        )
    }

    override fun headers(url: String): Map<String, String> {
        return mapOf(
            "Referer" to url
        )
    }
}