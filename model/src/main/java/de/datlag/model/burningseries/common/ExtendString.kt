@file:Obfuscate

package de.datlag.model.burningseries.common

import io.michaelrocks.paranoid.Obfuscate

fun String.encodeToHref(): String {
	val normalizedTitle = this.trim()
	var replaced = normalizedTitle.replace("(['`´’☆ōáÁ/.]+)".toRegex(), "-")

	if (replaced.startsWith("-")) {
		replaced = replaced.substring(1)
	}
	replaced = replaced.replace("([ü]+)".toRegex(), "ue")
	replaced = replaced.replace("([ö]+)".toRegex(), "oe")
	replaced = replaced.replace("([ä]+)".toRegex(), "ae")

	replaced = replaced.replace("([Ü]+)".toRegex(), "Ue")
	replaced = replaced.replace("([Ö]+)".toRegex(), "Oe")
	replaced = replaced.replace("([Ä]+)".toRegex(), "Ae")

	val regex = Regex("\\w*\\s*[_-]*")
	val allMatches = regex.findAll(replaced).map { it.value }
	val joinedMatches = allMatches.joinToString("").replace(Regex("\\s+"), "-").replace("_", "-")
	var encoded = joinedMatches.replace("(?:(-))+".toRegex(), "-")
	if (encoded.startsWith('-')) {
		encoded = encoded.substring(1)
	}
	if (encoded.endsWith('-')) {
		encoded = encoded.substring(0..(encoded.length - 2))
	}
	return encoded
}

fun String.getDigitsOrNull(): String? {
	val replaced = this.replace("\\D+".toRegex(), String())
	return if (replaced.isEmpty()) {
		null
	} else {
		replaced
	}
}
