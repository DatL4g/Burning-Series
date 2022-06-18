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

	const val API_BS_TO_BASE = "${PROTOCOL_HTTPS}${HOST_BS_TO}"

	const val API_GITHUB = "${PROTOCOL_HTTPS}api.${HOST_GITHUB}"

	const val BS_TO_HEADER = "${API_BS_TO_BASE}/public/images/header.png"
	const val URL_ADBLOCK_LIST = "https://raw.githubusercontent.com/Openadblockserverlist/adblockserverlist/master/adblockserverlist.txt"

	const val DATABASE_BURNING_SERIES = "BurningSeriesDatabase"

	const val NAMED_JSON = "JSON"
	const val NAMED_JSON_CONVERTER = "JSON_CONVERTER"
	const val NAMED_JSON_RETROFIT = "JSON_RETROFIT"

	const val HOUR_IN_SECONDS = 60 * 60
	const val DAY_IN_SECONDS = HOUR_IN_SECONDS * 24
	const val WEEK_IN_SECONDS = DAY_IN_SECONDS * 7

	const val GITHUB_OWNER = "DatL4g"
	const val GITHUB_REPO = "BurningSeries-Android"
	const val GITHUB_PROJECT = "${PROTOCOL_HTTPS}${HOST_GITHUB}/${GITHUB_OWNER}/${GITHUB_REPO}"
	const val GITHUB_SPONSOR = "${PROTOCOL_HTTPS}${HOST_GITHUB}/sponsors/${GITHUB_OWNER}"

	const val MAL_OAUTH_AUTH_URI = "${PROTOCOL_HTTPS}${HOST_MAL}/v1/oauth2/authorize"
	const val MAL_OAUTH_TOKEN_URI = "${PROTOCOL_HTTPS}${HOST_MAL}/v1/oauth2/token"
	const val MAL_RESPONSE_TYPE = "code"
	const val MAL_REDIRECT_URI = "datlag://burningseries/myanimelist"

	const val ANILIST_OAUTH_AUTH_URI = "${PROTOCOL_HTTPS}${HOST_ANILIST}/api/v2/oauth/authorize"
	const val ANILIST_OAUTH_TOKEN_URI = "${PROTOCOL_HTTPS}${HOST_ANILIST}/api/v2/oauth/token"
	const val ANILIST_RESPONSE_TYPE = "code"
	const val ANILIST_REDIRECT_URI = "datlag://burningseries/anilist"

	const val GITHUB_OAUTH_AUTH_URI = "${PROTOCOL_HTTPS}${HOST_GITHUB}/login/oauth/authorize"
	const val GITHUB_OAUTH_TOKEN_URI = "${PROTOCOL_HTTPS}${HOST_GITHUB}/login/oauth/access_token"
	const val GITHUB_RESPONSE_TYPE = "code"
	const val GITHUB_REDIRECT_URI = "datlag://burningseries/github"

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