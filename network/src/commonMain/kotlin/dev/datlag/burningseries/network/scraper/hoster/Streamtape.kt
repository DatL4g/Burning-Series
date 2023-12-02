package dev.datlag.burningseries.network.scraper.hoster

import dev.datlag.burningseries.model.common.listFrom
import dev.datlag.burningseries.network.scraper.Video
import ktsoup.KtSoupDocument

class Streamtape : Manipulation {
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
                val partValues = "[\"']?(\\S+)[\"']\\S*\\s*\\([\"'](\\S+)[\"']".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)).find(part)?.groupValues
                val p1 = partValues?.get(1)
                val p2 = partValues?.get(2)
                var p3 = 0
                if (part.contains("substring")) {
                    "substring\\((\\d+)".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)).findAll(part).forEach { result ->
                        result.groupValues.getOrNull(1)?.trim()?.toIntOrNull()?.let {
                            p3 += it
                        }
                    }
                }
                p1?.let {
                    append(it)
                }
                p2?.let {
                    append(it.substring(p3))
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