package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.common.ifTrue
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun CountryImage(
    code: String,
    description: String?,
    iconSize: Dp? = null,
) {
    val res = remember(code) { dev.datlag.burningseries.shared.ui.theme.CountryImage.getByFlag(code) }
    var iconWidth by remember { mutableFloatStateOf(iconSize?.value ?: 24.dp.value) }
    var iconHeight by remember { mutableFloatStateOf(iconSize?.value ?: 24.dp.value) }

    Box(
        modifier = Modifier.ifTrue(iconSize == null) {
            this.onSizeChanged {
                val thirdWidth = it.width.toFloat() - (it.width.toFloat() / 2F)
                val thirdHeight = it.height.toFloat() - (it.height.toFloat() / 2F)
                iconWidth = thirdWidth
                iconHeight = thirdHeight
            }
        }.ifTrue(res.size >= 2) {
            size(width = (iconWidth * 1.5).dp, height = (iconHeight * 1.5).dp)
        },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(res.last()),
            contentDescription = description,
            modifier = Modifier
                .size(width = iconWidth.dp, height = iconHeight.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .border(1.dp, LocalContentColor.current, MaterialTheme.shapes.extraSmall)
                .ifTrue(res.size >= 2) { this.align(Alignment.TopStart).alpha(0.75F) }
        )
        if (res.size >= 2) {
            Image(
                painter = painterResource(res.first()),
                contentDescription = description,
                modifier = Modifier
                    .size(width = iconWidth.dp, height = iconHeight.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .border(1.dp, LocalContentColor.current, MaterialTheme.shapes.extraSmall)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}