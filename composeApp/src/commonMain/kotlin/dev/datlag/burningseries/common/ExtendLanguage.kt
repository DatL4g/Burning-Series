package dev.datlag.burningseries.common

import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.english
import dev.datlag.burningseries.composeapp.generated.resources.select_default_language
import dev.datlag.burningseries.composeapp.generated.resources.german
import dev.datlag.burningseries.composeapp.generated.resources.german_subtitle
import dev.datlag.burningseries.composeapp.generated.resources.japanese_subtitle
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.other.CountryImage
import dev.datlag.burningseries.settings.model.Language
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Language?.title: StringResource
    get() = when (this) {
        null -> Res.string.select_default_language
        is Language.English -> Res.string.english
        is Language.German.Default -> Res.string.german
        is Language.German.Subtitle -> Res.string.german_subtitle
        is Language.JapaneseSubtitle -> Res.string.japanese_subtitle
    }

val Home.Episode.Flag.languageByCode: StringResource?
    get() = when {
        this.bestCountryCode.equals(Language.English.code, ignoreCase = true) -> Res.string.english
        this.bestCountryCode.equals(Language.German.Default.code, ignoreCase = true) -> Res.string.german
        this.bestCountryCode.equals(Language.German.Subtitle.code, ignoreCase = true) -> Res.string.german_subtitle
        this.bestCountryCode.equals(Language.JapaneseSubtitle.code, ignoreCase = true) -> Res.string.japanese_subtitle
        else -> null
    }

val Language?.flags: ImmutableSet<DrawableResource>
    get() = CountryImage.getByFlag(this?.code)