package de.datlag.network.video

import android.net.Uri
import android.util.Log
import de.datlag.model.burningseries.stream.Stream
import de.datlag.model.video.VideoStream
import de.datlag.network.common.getSrc
import de.datlag.network.common.toInt
import dev.datlag.jsunpacker.JsUnpacker
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import org.jsoup.Jsoup


@Obfuscate
class VideoScraper {

    fun scrapeVideosFrom(stream: Stream, preferMp4: Boolean): Flow<VideoStream?> = channelFlow {
        val parsed = Uri.parse(stream.url).buildUpon().appendQueryParameter("autoplay", "1").build()

        val doc = try {
            Jsoup.connect(parsed?.toString() ?: stream.url).get()
        } catch (ignored: Throwable) {
            try {
                Jsoup.connect(stream.url).get()
            } catch (ignored: Throwable) {
                return@channelFlow send(null)
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
        val regexWithQueryParams = "${regex.pattern}(\\?\\w+=(\\w|-)*(?:&(?:\\w+=(\\w|[-_.~%])*|=(\\w|[-_.~%])+))*)?".toRegex(
            setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        )
        regex.findAll(html).forEach {
            srcList.add(it.value)
        }
        regexWithQueryParams.findAll(html).forEach {
            srcList.add(it.value)
        }

        JsUnpacker.unpack(doc.select("script").map { it.data().trim() }).forEach {
            srcList.addAll(regex.findAll(it).map { result -> result.value })
            srcList.addAll(regexWithQueryParams.findAll(it).map { result -> result.value })
            if (it.contains("video", true)) {
                val dllRegex = Regex(
                    "http(s?)://\\S+\\.(dll)",
                    setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
                )
                srcList.addAll(dllRegex.findAll(it).map { result -> result.value })
            }
        }

        val finalList = if (stream.hoster.equals("StreamZ", true) || stream.hoster.equals("StreamZZ", true)) {
            srcList.filterNot { it.contains("getlink") }
        } else {
            srcList.toList()
        }

        return@channelFlow if (srcList.isEmpty()) {
            send(null)
        } else {
            send(VideoStream(
                stream.hoster,
                stream.url,
                finalList.sortedByDescending {
                    try {
                        Uri.parse(it).buildUpon().clearQuery().toString().endsWith(if (preferMp4) ".mp4" else ".m3u8", true).toInt()
                    } catch (ignored: Throwable) {
                        0
                    }
                },
                stream.config
            ))
        }
    }.flowOn(Dispatchers.IO)
}