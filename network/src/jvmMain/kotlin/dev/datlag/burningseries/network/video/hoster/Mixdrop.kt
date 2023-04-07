package dev.datlag.burningseries.network.video.hoster

import dev.datlag.burningseries.model.HosterStream
import dev.datlag.burningseries.network.video.VideoStreamManipulation
import dev.datlag.jsunpacker.JsUnpacker
import org.jsoup.nodes.Document

class Mixdrop : VideoStreamManipulation {
    override fun match(hosterStream: HosterStream): Boolean {
        return hosterStream.hoster.equals("Mixdrop", true)
    }

    override fun find(doc: Document): List<String> {
        return JsUnpacker.unpack(doc.select("script").map { it.data().trim() }).flatMap {
            "wurl=\\s*\"(.*?)\"".toRegex().findAll(it).toList().mapNotNull { result ->
                val url = result.groups[1]?.value?.trim()?.ifEmpty { null }
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
    }

    override fun remove(source: List<String>): List<String> {
        return source
    }

    override fun headers(source: Map<String, String>, hosterStream: HosterStream): Map<String, String> {
        val new = source.toMutableMap()
        new["Referer"] = hosterStream.url
        return new
    }
}