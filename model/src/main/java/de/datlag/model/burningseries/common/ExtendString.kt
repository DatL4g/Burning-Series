@file:Obfuscate

package de.datlag.model.burningseries.common

import io.michaelrocks.paranoid.Obfuscate

fun String.encodeToHref(): String {
	val normalizedTitle = this.trim()
	val replaced = normalizedTitle.replace("(['`´’☆ō/]+)".toRegex(), "-")

	val regex = Regex("\\w*\\s*[_-]*")
	val allMatches = regex.findAll(replaced).map { it.value }
	return allMatches.joinToString("").replace(Regex("\\s+"), "-").replace("_", "-")
}