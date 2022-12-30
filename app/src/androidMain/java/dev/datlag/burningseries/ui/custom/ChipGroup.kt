package dev.datlag.burningseries.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow

@Composable
actual fun ChipGroup(
    modifier: Modifier,
    horizontalSpace: Dp,
    verticalAlignment: Alignment.Vertical,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier,
        mainAxisSpacing = horizontalSpace,
        crossAxisAlignment = when (verticalAlignment) {
            Alignment.CenterVertically -> FlowCrossAxisAlignment.Center
            Alignment.Bottom -> FlowCrossAxisAlignment.End
            else -> FlowCrossAxisAlignment.Start
        }
    ) {
        content()
    }
}