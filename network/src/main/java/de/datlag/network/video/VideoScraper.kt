package de.datlag.network.video

import android.net.Uri
import de.datlag.network.common.getSrc
import io.michaelrocks.paranoid.Obfuscate
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Obfuscate
class VideoScraper {

    fun scrapeVideosFrom(url: String): List<String> {
        val parsed = Uri.parse(url).buildUpon().appendQueryParameter("autoplay", "1").build()
        val doc: Document = try {
            Jsoup.connect(parsed?.toString() ?: url).get()
        } catch (ignored: Exception) {
            try {
                Jsoup.connect(url).get()
            } catch (ignored: Exception) {
                return emptyList()
            }
        }

        val srcList: MutableSet<String> = mutableSetOf()
        val videoElements = doc.select("video")
        videoElements.forEach {
            it.getSrc()?.let { src -> srcList.add(src) }
        }
        val html = doc.html()
        val regex = Regex(
            "http(s?)://\\S+\\.(mp4|m3u8|webm|mkv|flv|vob|drc|gifv|avi|((m?)(2?)ts)|mov|qt|wmv|yuv|rm((vb)?)|viv|asf|amv|m4p|m4v|mp2|mp((e)?)g|mpe|mpv|m2v|svi|3gp|3g2|mxf|roq|nsv|f4v|f4p|f4a|f4b)",
            setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        )
        val matches = regex.findAll(html)

        matches.forEach {
            srcList.add(it.value)
        }
        return srcList.toList()
    }
}