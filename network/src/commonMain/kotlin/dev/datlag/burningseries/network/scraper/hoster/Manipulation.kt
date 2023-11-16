package dev.datlag.burningseries.network.scraper.hoster

import ktsoup.KtSoupDocument

interface Manipulation {

    fun match(url: String): Boolean

    fun changeUrl(url: String): String = url

    fun change(initialList: Collection<String>, doc: KtSoupDocument): List<String>

    fun headers(url: String): Map<String, String>
}