package dev.datlag.burningseries.github.model

import dev.datlag.burningseries.github.UserAndReleaseQuery
import dev.datlag.tooling.getDigitsOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class UserAndRelease(
    val isSponsoring: Boolean,
    val hasStarred: Boolean,
    val release: Release?
) {

    constructor(info: UserAndReleaseQuery.Data) : this(
        isSponsoring = info.user?.let { user ->
            user.viewerIsSponsoring || user.isViewer
        } == true,
        hasStarred = info.repository?.viewerHasStarred == true,
        release = info.repository?.latestRelease?.let(UserAndRelease::Release)
    )

    constructor(release: dev.datlag.burningseries.github.model.Release) : this(
        isSponsoring = false,
        hasStarred = false,
        release = Release(release)
    )

    @Serializable
    data class Release(
        val url: String?,
        val tagName: String,
        val title: String?,
        val isPrerelease: Boolean,
        val isDraft: Boolean
    ) {

        @Transient
        val tagNumber = tagName.getDigitsOrNull()?.toIntOrNull()

        constructor(release: UserAndReleaseQuery.LatestRelease) : this(
            url = (release.url as? CharSequence)?.toString()?.ifBlank { null },
            tagName = release.tagName,
            title = release.name?.ifBlank { null },
            isPrerelease = release.isPrerelease,
            isDraft = release.isDraft
        )

        constructor(release: dev.datlag.burningseries.github.model.Release) : this(
            url = release.htmlUrl,
            tagName = release.tagName,
            title = release.title,
            isPrerelease = release.preRelease,
            isDraft = release.draft
        )
    }
}
