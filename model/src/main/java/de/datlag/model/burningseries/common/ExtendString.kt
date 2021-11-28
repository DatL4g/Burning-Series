@file:Obfuscate

package de.datlag.model.burningseries.common

import io.michaelrocks.paranoid.Obfuscate

fun String.encodeToHref(): String {
	val normalizedTitle = this.trim()
	val regexIm = Regex("i(['’])m", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
	val allIm = regexIm.findAll(normalizedTitle)
	var replacedIm = normalizedTitle
	allIm.map { it.groups[1] }.forEach { group ->
		group?.range?.let {
			replacedIm = replacedIm.replaceRange(it, "-")
		}
	}

	val regex = Regex("\\w*\\s*[_-]*")
	val allMatches = regex.findAll(replacedIm).map { it.value }
	return allMatches.joinToString("").replace(Regex("\\s+"), "-").replace("_", "-")
}