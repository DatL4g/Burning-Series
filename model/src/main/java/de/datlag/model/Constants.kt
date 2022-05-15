package de.datlag.model

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
object Constants {
	const val PROTOCOL_HTTP = "http://"
	const val PROTOCOL_HTTPS = "https://"
	
	const val MEDIATYPE_JSON = "application/json"
	const val MEDIATYPE_GITHUB_JSON = "application/vnd.github.v3+json"

	const val HOST_BS_TO = "bs.to"
	const val HOST_GITHUB = "github.com"
	const val HOST_MAL = "myanimelist.net"
	const val HOST_ANILIST = "anilist.co"

	const val API_M3O = "${PROTOCOL_HTTPS}api.m3o.com"
	const val API_JSONBASE = "${PROTOCOL_HTTPS}jsonbase.com"
	const val API_JSONBASE_PREFIX = "/bs-decaptcha"
	const val API_BS_TO_BASE = "${PROTOCOL_HTTPS}${HOST_BS_TO}"
	const val API_BS_TO_ALL = "${API_BS_TO_BASE}/andere-serien"
	const val API_WRAP_API_BASE = "${PROTOCOL_HTTPS}wrapapi.com"
	const val API_WRAP_API_PREFIX = "/use/DatLag/burning-series"
	const val API_WRAP_API_VIDEO_PREFIX = "/use/DatLag/videofetcher"
	const val API_GITHUB = "${PROTOCOL_HTTPS}api.${HOST_GITHUB}"

	const val BS_TO_HEADER = "${API_BS_TO_BASE}/public/images/header.png"
	const val URL_ADBLOCK_LIST = "https://raw.githubusercontent.com/Openadblockserverlist/adblockserverlist/master/adblockserverlist.txt"

	const val API_WRAP_API_HOME_VERSION = "0.2.1"
	const val API_WRAP_API_ALL_VERSION = "0.1.1"
	const val API_WRAP_API_SERIES_VERSION = "0.2.6"
	const val API_WRAP_API_DOWNLOAD_VIDEO = "0.1.2"

	const val DATABASE_BURNING_SERIES = "BurningSeriesDatabase"
	const val M3O_BURNING_SERIES_TABLE = "BurningSeries"

	const val NAMED_JSON = "JSON"
	const val NAMED_JSON_CONVERTER = "JSON_CONVERTER"
	const val NAMED_JSON_RETROFIT = "JSON_RETROFIT"

	const val DAY_IN_MILLI = 1000 * 60 * 60 * 24

	const val GITHUB_OWNER = "DatL4g"
	const val GITHUB_REPO = "BurningSeries-Android"
	const val GITHUB_PROJECT = "${PROTOCOL_HTTPS}${HOST_GITHUB}/${GITHUB_OWNER}/${GITHUB_REPO}"

	const val MAL_OAUTH_AUTH_URI = "${PROTOCOL_HTTPS}${HOST_MAL}/v1/oauth2/authorize"
	const val MAL_OAUTH_TOKEN_URI = "${PROTOCOL_HTTPS}${HOST_MAL}/v1/oauth2/token"
	const val MAL_RESPONSE_TYPE = "code"
	const val MAL_REDIRECT_URI = "datlag://burningseries/myanimelist"

	const val ANILIST_OAUTH_AUTH_URI = "${PROTOCOL_HTTPS}${HOST_ANILIST}/api/v2/oauth/authorize"
	const val ANILIST_OAUTH_TOKEN_URI = "${PROTOCOL_HTTPS}${HOST_ANILIST}/api/v2/oauth/token"
	const val ANILIST_RESPONSE_TYPE = "code"
	const val ANILIST_REDIRECT_URI = "datlag://burningseries/anilist"

	val OAUTH_BROWSER_DENY = listOf("com.vewd.core.integration.dia")

	const val LOG_FILE = "burningseries.log"

	const val F_DROID_PACKAGE_NAME = "org.fdroid.fdroid"
	const val F_DROID_PACKAGES_URL = "${PROTOCOL_HTTPS}f-droid.org/packages/"

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