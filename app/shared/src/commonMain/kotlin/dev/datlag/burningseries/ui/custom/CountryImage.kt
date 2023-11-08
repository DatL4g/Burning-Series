package dev.datlag.burningseries.ui.custom

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.ifTrue
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun CountryImage(
    code: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    val res = remember(code) { dev.datlag.burningseries.ui.theme.CountryImage.getByFlag(code) }
    var iconSize by remember { mutableStateOf(DpSize(24.dp, 24.dp)) }

    Box(
        modifier = modifier.onSizeChanged {
            val thirdWidth = it.width.toFloat() - (it.width.toFloat() / 3F)
            val thirdHeight = it.height.toFloat() - (it.height.toFloat() / 3F)
            iconSize = DpSize(thirdWidth.dp, thirdHeight.dp)
        },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(res.last()),
            contentDescription = description,
            modifier = Modifier
                .size(iconSize)
                .clip(MaterialTheme.shapes.extraSmall)
                .border(1.dp, LocalContentColor.current, MaterialTheme.shapes.extraSmall)
                .ifTrue(res.size >= 2) { this.align(Alignment.TopStart).alpha(0.75F) }
        )
        if (res.size >= 2) {
            Image(
                painter = painterResource(res.first()),
                contentDescription = description,
                modifier = Modifier
                    .size(iconSize)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .border(1.dp, LocalContentColor.current, MaterialTheme.shapes.extraSmall)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}