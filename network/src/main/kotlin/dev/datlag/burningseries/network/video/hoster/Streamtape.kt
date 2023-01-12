package dev.datlag.burningseries.network.video.hoster

import dev.datlag.burningseries.model.HosterStream
import dev.datlag.burningseries.network.video.VideoStreamManipulation
import org.jsoup.nodes.Document

class Streamtape : VideoStreamManipulation {
    override fun match(hosterStream: HosterStream): Boolean {
        return hosterStream.hoster.equals("Streamtape", true)
    }

    override fun find(doc: Document): List<String> {
        val linkText = doc.selectFirst("#ideoolink")?.text()?.trim()
        if (linkText.isNullOrEmpty()) {
            return emptyList()
        }
        var link = linkText.toString()
        while (link.startsWith("/get_video") || link.isEmpty()) {
            link = link.substring(1)
        }
        if (link.isEmpty()) {
            return emptyList()
        }
        return listOf(
            "https://streamtape.com$link"
        )
    }

    override fun remove(source: List<String>): List<String> {
        return source
    }
}