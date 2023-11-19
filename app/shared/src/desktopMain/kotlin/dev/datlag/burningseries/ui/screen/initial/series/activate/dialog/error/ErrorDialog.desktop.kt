package dev.datlag.burningseries.ui.screen.initial.series.activate.dialog.error

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.SharedRes
import dev.icerock.moko.resources.compose.stringResource

@Composable
actual fun ErrorDialog(component: ErrorComponent) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text(
            text = stringResource(SharedRes.strings.activate_error_title),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = stringResource(SharedRes.strings.activate_error_text),
            modifier = Modifier.padding(8.dp)
        )
        Button(
            onClick = {
                component.dismiss()
            },
            modifier = Modifier.padding(8.dp).align(Alignment.End)
        ) {
            Text(text = stringResource(SharedRes.strings.close))
        }
    }
}