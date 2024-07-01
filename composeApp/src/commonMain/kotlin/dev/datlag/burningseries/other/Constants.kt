package dev.datlag.burningseries.other

data object Constants {
    const val VLC_WEBSITE = "https://www.videolan.org/"

    const val GITHUB_OWNER_NAME = "DatL4g"
    const val GITHUB_REPO_NAME = "Burning-Series"

    const val GITHUB_OWNER = "https://github.com/$GITHUB_OWNER_NAME"
    const val GITHUB_REPO = "$GITHUB_OWNER/$GITHUB_REPO_NAME"
    const val GITHUB_RELEASE = "$GITHUB_REPO/releases/latest"

    const val SYNCING_DOMAIN = "burningseries.datlag"
    const val SYNCING_URL = "https://burningseries.datlag/sync/"

    data object Sponsor {
        const val GITHUB = "https://github.com/sponsors/DatL4g"
        const val POLAR = "https://polar.sh/DatL4g"
        const val PATREON = "https://patreon.com/datlag"
    }
}