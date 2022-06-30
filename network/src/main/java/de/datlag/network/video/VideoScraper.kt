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

        unpackJavascript(doc).forEach {
            val unpackedMatches = regex.findAll(it)
            unpackedMatches.forEach { result ->
                srcList.add(result.value)
            }
        }

        return srcList.toList()
    }

    private fun unpackJavascript(doc: Document): Set<String> {
        val packedRegex = Regex("eval[(]function[(]p,a,c,k,e,[r|d]?", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        val packedExtractRegex = Regex("[}][(]'(.*)', *(\\d+), *(\\d+), *'(.*?)'[.]split[(]'[|]'[)]", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        val unpackReplaceRegex = Regex("\\b\\w+\\b", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        val packedScripts = doc.select("script").mapNotNull {
            val data = it.data().trim()
            if (data.contains(packedRegex)) {
                data
            } else {
                null
            }
        }

        return packedScripts.flatMap {
            packedExtractRegex.findAll(it).mapNotNull { result ->

                val payload = result.groups[1]?.value
                val symtab = result.groups[4]?.value?.split('|')
                val radix = result.groups[2]?.value?.toIntOrNull() ?: 10
                val count = result.groups[3]?.value?.toIntOrNull()
                val unbaser = Unbaser(radix)

                if (symtab == null || count == null || symtab.size != count) {
                    null
                } else {
                    payload?.replace(unpackReplaceRegex) { match ->
                        val word = match.value
                        val unbased = symtab[unbaser.unbase(word)]
                        unbased.ifEmpty {
                            word
                        }
                    }
                }
            }
        }.toSet()
    }
}