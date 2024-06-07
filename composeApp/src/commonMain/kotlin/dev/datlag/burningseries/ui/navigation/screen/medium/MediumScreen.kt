package dev.datlag.burningseries.ui.navigation.screen.medium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.lighten
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.datlag.burningseries.LocalDarkMode
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.common.plus
import dev.datlag.burningseries.other.rememberIsTv
import dev.datlag.burningseries.ui.navigation.screen.medium.component.CoverSection
import dev.datlag.burningseries.ui.navigation.screen.medium.component.DescriptionSection
import dev.datlag.burningseries.ui.navigation.screen.medium.component.EpisodeItem
import dev.datlag.burningseries.ui.navigation.screen.medium.component.Toolbar
import dev.datlag.burningseries.ui.theme.SchemeTheme
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.focusScale
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun MediumScreen(component: MediumComponent, updater: SchemeTheme.Updater?) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            Toolbar(component)
        }
    ) { padding ->
        val isAnime by component.seriesIsAnime.collectAsStateWithLifecycle(component.initialIsAnime)
        val isAndroidPhone = Platform.isAndroidJvm && Platform.rememberIsTv()
        val episodes by component.episodes.collectAsStateWithLifecycle(persistentListOf())

        LazyColumn(
            modifier = Modifier.fillMaxSize().haze(LocalHaze.current),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(1.dp),
            contentPadding = padding.plus(PaddingValues(top = 16.dp))
        ) {
            item {
                CoverSection(
                    component = component,
                    updater = updater,
                    modifier = Modifier.fillParentMaxWidth().padding(horizontal = 16.dp)
                )
            }
            item {
                DescriptionSection(
                    component = component,
                    modifier = Modifier.fillParentMaxWidth()
                )
            }
            if (isAndroidPhone && isAnime) {
                item {
                    Text(text = "AniFlow")
                }
            }
            items(episodes.toImmutableList(), key = { it.href }) {
                EpisodeItem(
                    item = it,
                    modifier = Modifier.fillParentMaxWidth()
                )
            }
        }
    }
}