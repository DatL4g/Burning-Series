package dev.datlag.burningseries.shared.ui.screen.video

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent
import kotlinx.coroutines.flow.StateFlow

interface VideoComponent : Component {

    val series: Series
    val episode: StateFlow<Series.Episode>
    val streams: List<Stream>

    val selectedSubtitle: StateFlow<Subtitle?>
    val startingPos: StateFlow<Long>

    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    fun back()
    fun ended()
    fun lengthUpdate(millis: Long)
    fun progressUpdate(millis: Long)

    fun selectSubtitle(subtitles: List<Subtitle>)

    @Parcelize
    data class Subtitle(
        val code: String,
        val title: String
    ) : Parcelable
}