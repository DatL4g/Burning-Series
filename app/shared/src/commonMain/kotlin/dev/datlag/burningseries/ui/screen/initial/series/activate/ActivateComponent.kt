package dev.datlag.burningseries.ui.screen.initial.series.activate

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.burningseries.ui.screen.initial.series.activate.component.DialogConfig

interface ActivateComponent : Component {

    val episode: Series.Episode

    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    fun back()
    fun onScraped(data: String)
}