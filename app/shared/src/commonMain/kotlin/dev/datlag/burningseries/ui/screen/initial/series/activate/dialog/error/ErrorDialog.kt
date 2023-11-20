package dev.datlag.burningseries.ui.screen.initial.series.activate.dialog.error

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.icerock.moko.resources.compose.stringResource
import dev.datlag.burningseries.SharedRes

@Composable
expect fun ErrorDialog(component: ErrorComponent)