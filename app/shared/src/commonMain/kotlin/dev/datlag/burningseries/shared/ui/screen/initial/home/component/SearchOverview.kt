package dev.datlag.burningseries.shared.ui.screen.initial.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.haze
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.shared.LocalHaze
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.LocalPadding
import dev.datlag.burningseries.shared.ui.screen.initial.home.HomeComponent
import dev.datlag.burningseries.shared.common.onClick
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.screen.initial.home.HomeConfig
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun RowScope.SearchOverview(items: List<Genre.Item>, component: HomeComponent) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1F).haze(state = LocalHaze.current),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = LocalPadding(),
    ) {
        item {
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(SharedRes.strings.search),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        items(items) { item ->
            Text(
                modifier = Modifier.fillParentMaxWidth().clip(MaterialTheme.shapes.small).onClick {
                    component.itemClicked(HomeConfig.Series(item))
                }.padding(vertical = 16.dp),
                text = item.bestTitle,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    VerticalScrollbar(rememberScrollbarAdapter(listState))
}