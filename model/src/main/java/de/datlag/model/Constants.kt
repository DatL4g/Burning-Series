package de.datlag.model

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
object Constants {
	const val PROTOCOL_HTTP = "http://"
	const val PROTOCOL_HTTPS = "https://"
	
	const val MEDIATYPE_JSON = "application/json"
	
	const val API_M3O = "${PROTOCOL_HTTPS}api.m3o.com"
	const val API_JSONBASE = "${PROTOCOL_HTTPS}jsonbase.com"
	const val API_JSONBASE_PREFIX = "/bs-decaptcha"
	const val API_BS_TO_BASE = "${PROTOCOL_HTTPS}bs.to"
	const val API_BS_TO_ALL = "${API_BS_TO_BASE}/andere-serien"
	const val API_WRAP_API_BASE = "${PROTOCOL_HTTPS}wrapapi.com"
	const val API_WRAP_API_PREFIX = "/use/DatLag/burning-series"
	const val API_WRAP_API_VIDEO_PREFIX = "/use/DatLag/videofetcher"

	const val API_WRAP_API_HOME_VERSION = "0.1.0"
	const val API_WRAP_API_ALL_VERSION = "0.1.1"
	const val API_WRAP_API_SERIES_VERSION = "0.2.3"
	const val API_WRAP_API_DOWNLOAD_VIDEO = "0.1.2"

	const val DATABASE_BURNING_SERIES = "BurningSeriesDatabase"

	const val NAMED_JSON = "JSON"
	const val NAMED_JSON_CONVERTER = "JSON_CONVERTER"
	const val NAMED_JSON_RETROFIT = "JSON_RETROFIT"

	const val DAY_IN_MILLI = 1000 * 60 * 60 * 24
	
	fun getBurningSeriesLink(href: String): String {
		return if (!href.matches("^\\w+?://.*".toRegex())) {
			if (!href.startsWith("/")) {
				"${API_BS_TO_BASE}/$href"
			} else {
				"${API_BS_TO_BASE}${"(?!:|/{2,})(/.*)".toRegex().find(href)?.value}"
			}
		} else {
			href
		}
	}
}