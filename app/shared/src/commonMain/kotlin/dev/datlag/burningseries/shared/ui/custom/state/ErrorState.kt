package dev.datlag.burningseries.shared.ui.custom.state

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.LocalDarkMode
import dev.datlag.burningseries.shared.SharedRes
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ErrorState(text: String, customText: (@Composable () -> Unit)? = null, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val painterRes = if (LocalDarkMode.current) {
            SharedRes.images.sad_dark
        } else {
            SharedRes.images.sad_light
        }
        Image(
            modifier = Modifier.fillMaxWidth(0.7F),
            painter = painterResource(painterRes),
            contentDescription = text
        )
        Text(
            modifier = Modifier.fillMaxWidth(0.85F),
            text = text,
            fontWeight = FontWeight.SemiBold,
            softWrap = true,
            textAlign = TextAlign.Center
        )
        customText?.invoke()
        Button(
            onClick = {
                onRetry()
            }
        ) {
            Text(text = stringResource(SharedRes.strings.retry))
        }
    }
}

@Composable
fun ErrorState(
    text: StringResource,
    customText: (@Composable () -> Unit)? = null,
    onRetry: () -> Unit
) = ErrorState(text = stringResource(text), customText, onRetry)