package dev.datlag.burningseries.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.SportsKabaddi
import androidx.compose.material.icons.rounded.SupervisorAccount
import androidx.compose.material.icons.rounded.VideoCameraFront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.utils.AndroidWindowSizeFix
import dev.datlag.burningseries.model.Series
import io.github.aakira.napier.Napier

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Series.Info.display(
    modifier: Modifier = Modifier
) {
    AndroidWindowSizeFix {
        val mapped = remember(header) { InfoMapping.from(header) }
        val useIcon = when (calculateWindowSizeClass().widthSizeClass) {
            WindowWidthSizeClass.Compact -> true
            else -> false
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (useIcon && mapped != null) {
                Icon(
                    imageVector = mapped.imageVector,
                    contentDescription = header
                )
            } else {
                Text(
                    text = "${header}:",
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(
                text = data,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        }
    }
}

private sealed interface InfoMapping {
    val label: String
    val alternativeLabel: String
        get() = label

    val imageVector: ImageVector

    fun isInfo(value: String): Boolean {
        return when {
            value.equals(label, ignoreCase = true) -> true
            value.equals(alternativeLabel, ignoreCase = true) -> true
            else -> false
        }
    }

    data object Year : InfoMapping {
        override val label: String
            get() = "Produktionsjahre"

        override val alternativeLabel: String
            get() = "Produktionsjahr"

        override val imageVector: ImageVector
            get() = Icons.Rounded.DateRange

    }

    data object Actor : InfoMapping {
        override val label: String
            get() = "Hauptdarsteller"

        override val alternativeLabel: String
            get() = "Darsteller"

        override val imageVector: ImageVector
            get() = Icons.Rounded.SportsKabaddi
    }

    data object Producer : InfoMapping {
        override val label: String
            get() = "Produzenten"

        override val alternativeLabel: String
            get() = "Produzent"

        override val imageVector: ImageVector
            get() = Icons.Rounded.SupervisorAccount
    }

    data object Director : InfoMapping {
        override val label: String
            get() = "Regisseure"

        override val alternativeLabel: String
            get() = "Regisseur"

        override val imageVector: ImageVector
            get() = Icons.Rounded.VideoCameraFront
    }

    data object Author : InfoMapping {
        override val label: String
            get() = "Autoren"

        override val alternativeLabel: String
            get() = "Autor"

        override val imageVector: ImageVector
            get() = Icons.Rounded.Draw
    }

    companion object {
        fun from(value: String): InfoMapping? {
            return when {
                Year.isInfo(value) -> Year
                Actor.isInfo(value) -> Actor
                Producer.isInfo(value) -> Producer
                Director.isInfo(value) -> Director
                Author.isInfo(value) -> Author
                else -> null
            }
        }
    }
}