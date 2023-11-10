package dev.datlag.burningseries.ui.screen.initial.series.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.ui.custom.readmore.ReadMoreText
import dev.datlag.burningseries.ui.custom.readmore.ReadMoreTextOverflow
import dev.datlag.burningseries.ui.custom.readmore.ToggleArea
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun DescriptionText(description: String) {
    var expanded by remember { mutableStateOf(true) }

    ReadMoreText(
        text = description,
        expanded = expanded,
        onExpandedChange = {
            expanded = it
        },
        modifier = Modifier.fillMaxWidth(),
        readMoreText = stringResource(SharedRes.strings.read_more),
        readMoreColor = MaterialTheme.colorScheme.primary,
        readMoreFontWeight = FontWeight.SemiBold,
        readMoreMaxLines = 2,
        readMoreOverflow = ReadMoreTextOverflow.Ellipsis,
        readLessText = stringResource(SharedRes.strings.read_less),
        readLessColor = MaterialTheme.colorScheme.primary,
        readLessFontWeight = FontWeight.SemiBold,
        toggleArea = ToggleArea.All
    )
}