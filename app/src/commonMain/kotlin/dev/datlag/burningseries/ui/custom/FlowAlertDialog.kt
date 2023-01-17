package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FlowAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    buttons: @Composable (
        isRow: Boolean,
        (TextLayoutResult) -> Unit,
        (@Composable () -> Unit)?
    ) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = title,
        text = text,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        buttons = {
            var useColumn by remember(buttons) { mutableStateOf(false) }
            val textLayoutListener: (TextLayoutResult) -> Unit = { result ->
                if (result.lineCount > 1) {
                    useColumn = true
                }
            }

            if (useColumn) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    buttons(!useColumn, textLayoutListener, null)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    buttons(!useColumn, textLayoutListener) { Spacer(modifier = Modifier.weight(1F).defaultMinSize(minWidth = 16.dp)) }
                }
            }
        }
    )
}

@Composable
fun FlowAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (
        isRow: Boolean,
        (TextLayoutResult) -> Unit,
    ) -> Unit,
    dismissButton: @Composable (
        isRow: Boolean,
        (TextLayoutResult) -> Unit,
    ) -> Unit,
    neutralButton: @Composable (
        isRow: Boolean,
        (TextLayoutResult) -> Unit,
    ) -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
) {
    FlowAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = title,
        text = text,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    ) { isRow, textLayout, spacer ->
        if (isRow) {
            neutralButton(isRow, textLayout)
            spacer?.invoke()
            dismissButton(isRow, textLayout)
            confirmButton(isRow, textLayout)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                dismissButton(isRow, textLayout)
                Spacer(modifier = Modifier.weight(1F).defaultMinSize(minWidth = 16.dp))
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    neutralButton(isRow, textLayout)
                    confirmButton(isRow, textLayout)
                }
            }
        }
    }
}