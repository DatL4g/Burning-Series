package dev.datlag.burningseries.other

import dev.datlag.burningseries.composeapp.generated.resources.AniFlow
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.aniflow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed interface Project {

    val image: DrawableResource?
    val title: StringResource
    val subTitle: StringResource?

    val `package`: String
    val googlePlay: String?
    val github: String?

    data object AniFlow : Project {
        override val image: DrawableResource = Res.drawable.AniFlow
        override val title: StringResource = Res.string.aniflow
        override val subTitle: StringResource? = null

        override val `package`: String = "dev.datlag.aniflow"
        override val googlePlay: String = "https://play.google.com/store/apps/details?id=$`package`"
        override val github: String = "https://github.com/DatL4g/AniFlow"
    }
}