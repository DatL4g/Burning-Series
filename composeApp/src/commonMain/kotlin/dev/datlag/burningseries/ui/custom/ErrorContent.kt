package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalDarkMode
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import dev.datlag.burningseries.MokoRes
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.error_try_again
import dev.datlag.burningseries.composeapp.generated.resources.oh_noo

@Composable
fun ErrorContent(
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    painter: ImageResource = if (LocalDarkMode.current) MokoRes.images.donut_dark else MokoRes.images.donut_light,
    title: StringResource = Res.string.oh_noo,
    text: StringResource = Res.string.error_try_again
) {
    if (horizontal) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Image(
                modifier = Modifier.weight(1F),
                painter = painterResource(painter),
                contentDescription = null
            )
            Column(
                modifier = Modifier.weight(1.5F),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = stringResource(text))
            }
        }
    } else {
        Column(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(painter),
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(text),
                textAlign = TextAlign.Center
            )
        }
    }
}