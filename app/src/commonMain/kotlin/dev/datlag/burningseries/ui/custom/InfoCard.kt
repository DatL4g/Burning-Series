package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCard(
    title: String? = null,
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    icon: ImageVector = Icons.Default.Lightbulb,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title
            )
            Column {
                if (!title.isNullOrEmpty()) {
                    Text(
                        text = title,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = text
                )
            }
        }
    }
}