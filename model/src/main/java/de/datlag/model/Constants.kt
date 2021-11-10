package de.datlag.model

object Constants {
	const val PROTOCOL_HTTP = "http://"
	const val PROTOCOL_HTTPS = "https://"
	
	const val MEDIATYPE_JSON = "application/json"
	
	const val API_M3O = "${PROTOCOL_HTTPS}api.m3o.com"
	const val API_JSONBASE = "${PROTOCOL_HTTPS}jsonbase.com"
	const val API_BS_TO_BASE = "${PROTOCOL_HTTPS}bs.to"
	const val API_WRAP_API_BURNING_SERIES = "${PROTOCOL_HTTPS}wrapapi.com"
	const val API_WRAP_API_PREFIX = "/use/DatLag/burning-series"
	
	const val API_WRAP_API_KEY = "nzwmJafQfIjik6O1h2T68JaDmQqj56oJ"
	const val API_WRAP_API_HOME_VERSION = "0.1.0"
	const val API_WRAP_API_ALL_VERSION = "0.1.0"
	const val API_WRAP_API_SERIES_VERSION = "0.1.1"
	
	const val NAMED_JSON = "JSON"
	const val NAMED_JSON_CONVERTER = "JSON_CONVERTER"
	const val NAMED_JSON_RETROFIT = "JSON_RETROFIT"
	
	fun getBurningSeriesLink(href: String): String {
		return if (!href.matches("^\\w+?://.*".toRegex())) {
			if (!href.startsWith("/")) {
				"${PROTOCOL_HTTPS}bs.to/$href"
			} else {
				"${PROTOCOL_HTTPS}bs.to${"(?!:|/{2,})(/.*)".toRegex().find(href)?.value}"
			}
		} else {
			href
		}
	}
}