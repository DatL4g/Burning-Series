package dev.datlag.burningseries.other

import androidx.compose.runtime.Composable
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.aniflow
import org.jetbrains.compose.resources.StringResource
import dev.icerock.moko.resources.ImageResource
import dev.datlag.burningseries.MokoRes
import dev.datlag.tooling.Platform

sealed interface Project {

    val image: ImageResource?
    val title: StringResource
    val subTitle: StringResource?

    val `package`: String
    val googlePlay: String?
    val github: String?

    data object AniFlow : Project {
        override val image: ImageResource = MokoRes.images.AniFlow
        override val title: StringResource = Res.string.aniflow
        override val subTitle: StringResource? = null

        override val `package`: String = "dev.datlag.aniflow"
        override val googlePlay: String = "https://play.google.com/store/apps/details?id=$`package`"
        override val github: String = "https://github.com/DatL4g/AniFlow"
    }
}

@Composable
expect fun Platform.rememberIsTv(): Boolean