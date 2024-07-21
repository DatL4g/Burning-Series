package dev.datlag.burningseries.ui.navigation.screen.medium

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.database.CombinedEpisode
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.burningseries.other.K2Kast
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.kast.Device
import dev.datlag.kast.DeviceType
import dev.datlag.kast.Kast
import dev.datlag.kast.UnselectReason
import dev.datlag.skeo.DirectLink
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediumComponent : Component {
    val seriesData: SeriesData
    val initialIsAnime: Boolean

    val seriesState: StateFlow<SeriesState>
    val seriesTitle: Flow<String>
    val seriesSubTitle: Flow<String?>
    val seriesCover: Flow<String?>
    val seriesInfo: Flow<ImmutableCollection<Series.Info>>
    val seriesSeason: Flow<Series.Season?>
    val seriesSeasonList: Flow<ImmutableCollection<Series.Season>>
    val seriesLanguage: Flow<Series.Language?>
    val seriesLanguageList: Flow<ImmutableCollection<Series.Language>>
    val seriesDescription: Flow<String>
    val seriesIsAnime: Flow<Boolean>
    val combinedEpisodes: Flow<ImmutableCollection<CombinedEpisode>>
    val nextCombinedEpisode: Flow<CombinedEpisode?>

    val episodeState: StateFlow<EpisodeState>

    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    val isFavorite: StateFlow<Boolean>

    fun back()
    fun season(value: Series.Season)
    fun language(value: Series.Language)
    fun episode(episode: Series.Episode)
    fun episode(combinedEpisode: CombinedEpisode) = episode(combinedEpisode.default)
    fun watched(series: Series, episode: Series.Episode)
    fun watched(series: Series, combinedEpisode: CombinedEpisode) = watched(series, combinedEpisode.default)
    fun unwatched(series: Series, episode: Series.Episode)
    fun unwatched(series: Series, combinedEpisode: CombinedEpisode) = unwatched(series, combinedEpisode.default)
    fun activate(series: Series, episode: Series.Episode)
    fun activate(series: Series, episode: CombinedEpisode) = activate(series, episode.default)
    fun setFavorite(series: Series)
    fun unsetFavorite(series: Series)
    fun showSponsoringOrWatch(
        series: Series,
        episode: Series.Episode,
        streams: ImmutableCollection<DirectLink>
    )

    sealed interface Device {
        val icon: ImageVector
        val name: String
        val selected: Boolean

        fun select()

        data class Chrome(
            private val device: dev.datlag.kast.Device,
            override val icon: ImageVector = when (device.type) {
                is DeviceType.TV -> Icons.Rounded.Tv
                is DeviceType.SPEAKER -> Icons.Rounded.Speaker
                else -> Icons.Rounded.Devices
            },
            override val name: String = device.name,
            override val selected: Boolean = device.isSelected
        ) : Device {
            override fun select() {
                if (selected) {
                    Kast.unselect(UnselectReason.disconnected)
                } else {
                    K2Kast.disconnect()
                    Kast.select(device)
                }
            }
        }

        data class K2K(
            private val device: K2Kast.Device,
            override val icon: ImageVector = Icons.Rounded.Warning,
            override val name: String = "${device.name} (Experimental)",
            override val selected: Boolean = device.selected
        ) : Device {
            override fun select() {
                if (selected) {
                    K2Kast.disconnect()
                } else {
                    Kast.unselect(UnselectReason.disconnected)
                    K2Kast.connect(device)
                }
            }
        }
    }
}