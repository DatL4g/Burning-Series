package dev.datlag.burningseries.network.scraper.hoster

import dev.datlag.burningseries.model.common.listFrom
import dev.datlag.burningseries.network.scraper.Video
import ktsoup.KtSoupDocument

class StreamTape : Manipulation {
    override fun match(url: String): Boolean {
        return "(?://|\\.)(s(?:tr)?(?:eam|have)?(?:ta?p?e?|cloud|adblock(?:plus|er))\\.(?:com|cloud|net|pe|site|link|cc|online|fun|cash|to|xyz))/(?:e|v)/([0-9a-zA-Z]+)".toRegex().containsMatchIn(url)
    }

    override fun changeUrl(url: String): String {
        var newUrl = if (url.contains("/e/")) {
            url.replace("/e/", "/v/")
        } else {
            url
        }

        newUrl = if (newUrl.endsWith("mp4")) {
            newUrl.substringBeforeLast('/')
        } else {
            newUrl
        }
        return newUrl
    }

    override fun change(initialList: Collection<String>, doc: KtSoupDocument): List<String> {
        val pattern = "ById\\('.+?=\\s*([\"']//[^;<]+)".toRegex(RegexOption.IGNORE_CASE)
        val parts = pattern.find(doc.html())?.groupValues?.getOrNull(1)?.replace("'", "\"")?.split("\\+")
        if (parts.isNullOrEmpty()) {
            return initialList.toList()
        }

        val srcUrl = buildString {
            parts.forEach { part ->
                val p1 = "\"([^\"]*)".toRegex().find(part)?.groupValues?.get(1)
                var p2 = 0
                if (part.contains("substring")) {
                    "substring\\((\\d+)".toRegex(RegexOption.IGNORE_CASE).findAll(part).forEach { result ->
                        result.groupValues.getOrNull(1)?.trim()?.toIntOrNull()?.let {
                            p2 += it
                        }
                    }
                }
                p1?.let {
                    append(it.substring(p2))
                }
            }
            append("&stream=1")
        }
        val stream = if (srcUrl.startsWith("//")) {
            "https:$srcUrl"
        } else {
            srcUrl
        }
        return listFrom(
            listOf(stream),
            initialList
        )
    }

    override fun headers(url: String): Map<String, String> {
        return mapOf(
            "Referer" to Video.baseUrl(url)
        )
    }
}