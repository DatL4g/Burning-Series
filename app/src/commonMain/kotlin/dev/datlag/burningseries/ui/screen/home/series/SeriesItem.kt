package dev.datlag.burningseries.ui.screen.home.series

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.cardItemSize
import dev.datlag.burningseries.common.coverFileName
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.database.DBSeries
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.ui.custom.CoverImage
import dev.datlag.burningseries.ui.screen.SeriesItemComponent
import dev.datlag.burningseries.ui.screen.home.LocalFabGroupRequester
import java.io.File

@Composable
fun SeriesItem(content: Home.Series, component: SeriesItemComponent) {
    SeriesItem(
        content.href,
        content.title,
        content.cover,
        component
    )
}

@Composable
fun SeriesItem(content: DBSeries, component: SeriesItemComponent) {
    val base64 = remember(content.hrefTitle) { runCatching {
        File(component.imageDir, content.coverFileName()).readText()
    }.getOrNull() }

    SeriesItem(
        content.href,
        content.title,
        Cover(
            href = content.coverHref ?: String(),
            base64 = base64 ?: String()
        ),
        component
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesItem(
    href: String,
    title: String,
    cover: Cover,
    component: SeriesItemComponent
) {
    val fabGroup = LocalFabGroupRequester.current

    Card(modifier = Modifier.cardItemSize().onClick {
        component.onSeriesClicked(
            href,
            SeriesInitialInfo(title, cover)
        )
    }.focusProperties {
        if (fabGroup != null) {
            down = fabGroup
        }
    }) {
        CoverImage(
            cover = cover,
            description = title,
            scale = ContentScale.FillBounds,
            fallbackIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.cardItemSize().height(300.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            modifier = Modifier.cardItemSize().padding(8.dp)
        )
    }
}