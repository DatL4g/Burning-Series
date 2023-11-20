package dev.datlag.burningseries.network.scraper

import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.model.common.scopeCatching
import dev.datlag.burningseries.model.common.setFrom
import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.network.common.getSources
import dev.datlag.burningseries.network.scraper.hoster.MixDrop
import dev.datlag.burningseries.network.scraper.hoster.StreamTape
import dev.datlag.jsunpacker.JsUnpacker
import io.ktor.client.*
import io.ktor.http.*
import ktsoup.KtSoupDocument
import ktsoup.KtSoupParser
import ktsoup.parseRemote
import ktsoup.setClient

data object Video {

    private val MANIPULATION_LIST = setOf(
        MixDrop(),
        StreamTape()
    )

    suspend fun loadVideos(client: HttpClient, url: String): Stream? {
        KtSoupParser.setClient(client)

        val newUrl = MANIPULATION_LIST.mapNotNull {
            if (it.match(url)) {
                it
            } else {
                null
            }
        }.fold(url) { str, it ->
            it.changeUrl(str)
        }

        val doc = suspendCatching {
            KtSoupParser.parseRemote(newUrl)
        }.getOrNull() ?: return null

        val docStreams = getStreamsInDoc(doc)
        val iframeStreams = doc.querySelectorAll("iframe")
            .asSequence()
            .flatMap { it.getSources() }
            .toSet()
            .flatMap { src ->
                val iframeDoc = KtSoupParser.parse(src)
                getStreamsInDoc(iframeDoc)
            }

        val (streams, headers) = applyHosterManipulation(
            newUrl,
            setFrom(
                docStreams,
                iframeStreams
            ),
            doc
        )

        return if (streams.isEmpty()) {
            null
        } else {
            Stream(streams.toSet().toList(), headers)
        }
    }

    private suspend fun getStreamsInDoc(doc: KtSoupDocument): Set<String> {
        val videoElements = doc.querySelectorAll("video").map {
            it.getSources()
        }.flatten()
        val html = doc.html()
        val regex = Regex(
            "http(s?)://\\S+\\.(mp4|m3u8|webm|mkv|flv|vob|drc|gifv|avi|((m?)(2?)ts)|mov|qt|wmv|yuv|rm((vb)?)|viv|asf|amv|m4p|m4v|mp2|mp((e)?)g|mpe|mpv|m2v|svi|3gp|3g2|mxf|roq|nsv|f4v|f4p|f4a|f4b|dll)",
            setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        )
        val regexWithQueryParams = "${regex.pattern}(\\?\\w+=(\\w|-)*(?:&(?:\\w+=(\\w|[-_.~%])*|=(\\w|[-_.~%])+))*)?".toRegex(
            setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        )

        val regexResult = regex.findAll(html).map {
            it.value
        }.toList()
        val regexWithQueryResult = regexWithQueryParams.findAll(html).map {
            it.value
        }.toList()

        val jsResult = JsUnpacker.unpack(doc.querySelectorAll("script").map { it.html() }).flatMap {
            val jsRegexResult = regex.findAll(it).map { result -> result.value }.toList()
            val jsRegexWithQueryResult = regexWithQueryParams.findAll(it).map { result -> result.value }.toList()

            setFrom(
                jsRegexResult,
                jsRegexWithQueryResult
            )
        }

        return setFrom(
            videoElements,
            regexResult,
            regexWithQueryResult,
            jsResult
        )
    }

    private suspend fun applyHosterManipulation(
        url: String,
        initialList: Collection<String>,
        doc: KtSoupDocument
    ): Pair<Collection<String>, Map<String, String>> {
        val manipulator = MANIPULATION_LIST.mapNotNull {
            if (it.match(url)) {
                it
            } else {
                null
            }
        }

        return (manipulator.fold(initialList) { list, it ->
            it.change(list, doc)
        }) to (manipulator.map { it.headers(url) }.flatMap { it.entries }.associate { it.key to it.value })
    }

    internal fun baseUrl(url: String): String {
        return scopeCatching {
            val newUrl = URLBuilder(url)
            newUrl.user = null
            newUrl.password = null
            newUrl.pathSegments = emptyList()
            newUrl.encodedParameters = ParametersBuilder(0)
            newUrl.fragment = String()
            newUrl.trailingQuery = true
            newUrl.buildString()
        }.getOrNull() ?: run {
            val regex = "^(?:https?://)?(?:[^@\\n]+@)?(?:www\\.)?([^:/\\n?]+)".toRegex(RegexOption.IGNORE_CASE)
            regex.find(url)?.value
        } ?: url
    }
}