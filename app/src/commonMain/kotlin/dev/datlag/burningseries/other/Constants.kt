package dev.datlag.burningseries.other

object Constants {

    const val PROTOCOL_HTTP = "http://"
    const val PROTOCOL_HTTPS = "https://"

    const val HOST_BS_TO = "bs.to"
    const val HOST_GITHUB_COM = "github.com"

    const val LINUX_DARK_MODE_CMD = "gsettings get org.gnome.desktop.interface color-scheme"
    const val LINUX_DARK_MODE_LEGACY_CMD = "gsettings get org.gnome.desktop.interface gtk-theme"

    const val GITHUB_OWNER = "DatL4g"
    const val GITHUB_REPO = "BurningSeries"
    const val GITHUB_REPOSITORY_URL = "${PROTOCOL_HTTPS}${HOST_GITHUB_COM}/$GITHUB_OWNER/$GITHUB_REPO"

    const val VLC_DOWNLOAD_URL = "${PROTOCOL_HTTPS}www.videolan.org/vlc/#download"


    fun getBurningSeriesUrl(href: String): String {
        return if (!href.matches("^\\w+?://.*".toRegex())) {
            if (!href.startsWith("/")) {
                "${PROTOCOL_HTTPS}${HOST_BS_TO}/$href"
            } else {
                "${PROTOCOL_HTTPS}${HOST_BS_TO}${"(?!:|/{2,})(/.*)".toRegex().find(href)?.value}"
            }
        } else {
            href
        }
    }
}