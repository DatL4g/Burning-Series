@file:Obfuscate

package de.datlag.network.common

import io.michaelrocks.paranoid.Obfuscate
import org.jsoup.nodes.Element

fun Element.getTitle(): String {
    return if (this.hasAttr("title")) {
        this.attr("title")
    } else {
        this.text()
    } ?: String()
}

fun Element.getHref(): String? {
    return if (this.hasAttr("href")) {
        this.attr("href")
    } else {
        null
    }
}

fun Element.getSrc(): String? {
    return if (this.hasAttr("src")) {
        this.attr("src")
    } else {
        null
    }
}

fun Element.getValue(): String? {
    return if (this.hasAttr("value")) {
        this.attr("value")
    } else {
        null
    }
}