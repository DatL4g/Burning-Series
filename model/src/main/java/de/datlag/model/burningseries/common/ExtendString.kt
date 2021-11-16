@file:Obfuscate

package de.datlag.model.burningseries.common

import io.michaelrocks.paranoid.Obfuscate

fun String.encodeToHref(): String {
	val normalizedTitle = this.trim()
	val regex = Regex("\\w*\\s*[_-]*")
	val allMatches = regex.findAll(normalizedTitle).map { it.value }
	return allMatches.joinToString("").replace(Regex("\\s+"), "-").replace("_", "-")
}