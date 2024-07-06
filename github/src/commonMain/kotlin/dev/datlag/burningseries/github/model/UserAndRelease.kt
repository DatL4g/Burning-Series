package dev.datlag.burningseries.github.model

import dev.datlag.burningseries.github.UserAndReleaseQuery
import dev.datlag.burningseries.model.serializer.SerializableImmutableSet
import dev.datlag.tooling.getDigitsOrNull
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class UserAndRelease(
    val user: User?,
    val release: Release?
) {

    constructor(info: UserAndReleaseQuery.Data) : this(
        user = User(info),
        release = info.repository?.latestRelease?.let(UserAndRelease::Release)
    )

    constructor(release: RESTRelease) : this(
        user = null,
        release = Release(release)
    )

    @Serializable
    data class User(
        val isSponsoring: Boolean,
        val hasStarred: Boolean,
        val avatar: String?,
        private val _name: String?,
        val login: String
    ) {

        @Transient
        val name: String = _name?.ifBlank { null } ?: login

        constructor(info: UserAndReleaseQuery.Data) : this(
            isSponsoring = info.user?.viewerIsSponsoring == true || info.user?.isViewer == true,
            hasStarred = info.repository?.viewerHasStarred == true,
            avatar = (info.viewer.avatarUrl as? CharSequence)?.toString()?.ifBlank { null },
            _name = info.viewer.name?.ifBlank { null },
            login = info.viewer.login
        )
    }

    @Serializable
    data class Release(
        val url: String?,
        val tagName: String,
        val title: String?,
        val isPrerelease: Boolean,
        val isDraft: Boolean,
        val assets: SerializableImmutableSet<Asset> = persistentSetOf()
    ) {

        @Transient
        val tagNumber = tagName.getDigitsOrNull()?.toIntOrNull()

        @Transient
        val androidAsset = assets.maxByOrNull { it.apkIdentifier }?.let { a ->
            if (a.hasAnyApkIdentifier) {
                a
            } else {
                null
            }
        }

        constructor(release: UserAndReleaseQuery.LatestRelease) : this(
            url = (release.url as? CharSequence)?.toString()?.ifBlank { null },
            tagName = release.tagName,
            title = release.name?.ifBlank { null },
            isPrerelease = release.isPrerelease,
            isDraft = release.isDraft,
            assets = release.releaseAssets.nodesFilterNotNull()?.map(::Asset).orEmpty().toImmutableSet()
        )

        constructor(release: RESTRelease) : this(
            url = release.htmlUrl,
            tagName = release.tagName,
            title = release.title,
            isPrerelease = release.preRelease,
            isDraft = release.draft,
            assets = release.assets
        )

        fun asUpdateOrNull(currentVersion: String?): Release? {
            if (currentVersion.isNullOrBlank()) {
                return null
            }
            val releaseNumber = tagNumber ?: return null
            val currentNumber = currentVersion.getDigitsOrNull()?.toIntOrNull() ?: return null

            if (releaseNumber > currentNumber) {
                return this
            }
            return null
        }
    }
}
