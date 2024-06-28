package dev.datlag.burningseries.other

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.ImageResource
import dev.datlag.burningseries.MokoRes
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.aniflow
import dev.datlag.burningseries.composeapp.generated.resources.aniflow_subtitle
import org.jetbrains.compose.resources.StringResource

data object AniFlow {
    val icon: ImageResource = MokoRes.images.AniFlow
    val title: StringResource = Res.string.aniflow
    val subTitle: StringResource = Res.string.aniflow_subtitle
    const val packageName = "dev.datlag.aniflow"
    const val googlePlay = "https://play.google.com/store/apps/details?id=$packageName"
}

@Composable
expect fun AniFlow.isInstalled(): Boolean