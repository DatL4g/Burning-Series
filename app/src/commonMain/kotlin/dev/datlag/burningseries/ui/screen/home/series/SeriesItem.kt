package dev.datlag.burningseries.ui.screen.home.series

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.ui.custom.CoverImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SeriesItem(content: Home.Series, component: SeriesComponent) {
    Card(modifier = Modifier.fillMaxWidth().onClick {
        component.onSeriesClicked(
            content.href,
            SeriesInitialInfo(content.title, content.cover)
        )
    }) {
        CoverImage(
            cover = content.cover,
            description = content.title,
            scale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )
        Text(
            text = content.title,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
    }
}