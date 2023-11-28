package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.success

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.icerock.moko.resources.compose.stringResource
import dev.datlag.burningseries.shared.SharedRes

@Composable
expect fun SuccessDialog(component: SuccessComponent)