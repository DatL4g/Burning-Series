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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.lighten
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.datlag.burningseries.LocalDarkMode
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.common.plus
import dev.datlag.burningseries.common.rememberIsTv
import dev.datlag.burningseries.common.scrollUpVisible
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.burningseries.other.AniFlow
import dev.datlag.burningseries.other.isInstalled
import dev.datlag.burningseries.ui.custom.ErrorContent
import dev.datlag.burningseries.ui.navigation.screen.medium.component.AniFlowCard
import dev.datlag.burningseries.ui.navigation.screen.medium.component.CoverSection
import dev.datlag.burningseries.ui.navigation.screen.medium.component.DescriptionSection
import dev.datlag.burningseries.ui.navigation.screen.medium.component.EpisodeItem
import dev.datlag.burningseries.ui.navigation.screen.medium.component.SeasonLanguageSection
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
    val episodeState by component.episodeState.collectAsStateWithLifecycle()
    val dialogState by component.dialog.subscribeAsState()
    val seriesState by component.seriesState.collectAsStateWithLifecycle()

    when (val current = episodeState) {
        is EpisodeState.SuccessStream -> {
            component.showSponsoringOrWatch(
                series = (seriesState as SeriesState.Success).series,
                episode = current.episode,
                streams = current.results
            )
        }
        is EpisodeState.ErrorHoster -> {
            component.activate(
                series = (seriesState as SeriesState.Success).series,
                episode = current.episode
            )
        }
        is EpisodeState.ErrorStream -> {
            component.activate(
                series = (seriesState as SeriesState.Success).series,
                episode = current.episode
            )
        }
        else -> { }
    }
    val loadingEpisode = remember(episodeState) {
        when (val current = episodeState) {
            is EpisodeState.Loading -> current.episode
            is EpisodeState.SuccessHoster -> current.episode
            else -> null
        }
    }

    dialogState.child?.instance?.render()

    Scaffold(
        topBar = {
            Toolbar(
                component = component,
                series = (seriesState as? SeriesState.Success)?.series
            )
        },
        floatingActionButton = {
            val nextEpisode by component.nextCombinedEpisode.collectAsStateWithLifecycle(null)

            nextEpisode?.ifHasHoster()?.let { episode ->
                ExtendedFloatingActionButton(
                    onClick = {
                        component.episode(episode)
                    },
                    expanded = listState.scrollUpVisible(),
                    icon = {
                        if (episode.isSame(loadingEpisode)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null
                            )
                        }
                    },
                    text = {
                        Text(
                            text = episode.mainTitle,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    ) { padding ->
        val isAnime by component.seriesIsAnime.collectAsStateWithLifecycle(component.initialIsAnime)
        val isAndroidPhone = Platform.isAndroidJvm && !Platform.rememberIsTv()
        val isAniFlowInstalled = AniFlow.isInstalled()
        val episodes by component.combinedEpisodes.collectAsStateWithLifecycle(persistentListOf())

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
                SeasonLanguageSection(
                    component = component,
                    modifier = Modifier.fillParentMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp)
                )
            }
            if (isAndroidPhone && isAnime && !isAniFlowInstalled) {
                item {
                    AniFlowCard(modifier = Modifier.fillParentMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp))
                }
            }
            item {
                DescriptionSection(
                    component = component,
                    modifier = Modifier.fillParentMaxWidth().padding(vertical = 8.dp)
                )
            }
            if (seriesState is SeriesState.Failure) {
                item {
                    ErrorContent(
                        modifier = Modifier.fillParentMaxWidth()
                    )
                }
            }
            items(episodes.toImmutableList(), key = { it.href }) {
                EpisodeItem(
                    item = it,
                    isLoading = it.isSame(loadingEpisode),
                    modifier = Modifier.fillParentMaxWidth(),
                    onClick = component::episode,
                    onMarkWatched = { c ->
                        component.watched(
                            series = (seriesState as SeriesState.Success).series,
                            combinedEpisode = c
                        )
                    },
                    onMarkUnwatched = { c ->
                        component.unwatched(
                            series = (seriesState as SeriesState.Success).series,
                            combinedEpisode = c
                        )
                    },
                    onActivate = { c ->
                        component.activate(
                            series = (seriesState as SeriesState.Success).series,
                            episode = c
                        )
                    }
                )
            }
        }
    }
}