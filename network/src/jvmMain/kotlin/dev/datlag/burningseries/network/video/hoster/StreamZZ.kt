package dev.datlag.burningseries.network.video.hoster

import dev.datlag.burningseries.model.HosterStream
import dev.datlag.burningseries.network.video.VideoStreamManipulation
import org.jsoup.nodes.Document

class StreamZZ : VideoStreamManipulation {

    override fun match(hosterStream: HosterStream): Boolean {
        return hosterStream.hoster.equals("StreamZ", true) || hosterStream.hoster.equals("StreamZZ", true)
    }

    override fun find(doc: Document): List<String> {
        return emptyList()
    }

    override fun remove(source: List<String>): List<String> {
        return source.filterNot { it.contains("getlink") }
    }
}