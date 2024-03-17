package dev.datlag.burningseries.shared.other

import dev.datlag.burningseries.shared.SharedRes
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

sealed interface Project {

    val icon: ImageResource?
    val title: StringResource
    val subTitle: StringResource

    val `package`: String
    val googlePlay: String?
    val github: String?

    data object PULZ : Project {
        override val icon: ImageResource = SharedRes.images.PulZ
        override val title: StringResource = SharedRes.strings.pulz
        override val subTitle: StringResource = SharedRes.strings.pulz_subtitle

        override val `package`: String = "dev.datlag.pulz"
        override val googlePlay: String = "https://play.google.com/store/apps/details?id=$`package`"
        override val github: String = "https://github.com/DatL4g/PulZ"
    }

    data object AniFlow : Project {
        override val icon: ImageResource = SharedRes.images.ayaya
        override val title: StringResource = SharedRes.strings.aniflow
        override val subTitle: StringResource = SharedRes.strings.aniflow_subtitle
        override val `package`: String = "dev.datlag.aniflow"
        override val googlePlay: String? = null
        override val github: String = "https://github.com/DatL4g/AniFlow"
    }
}