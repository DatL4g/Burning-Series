package dev.datlag.burningseries.ui.screen.activate

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.navigation.Component

interface ActivateComponent : Component {

    val episode: Series.Episode
    val series: Series
    val onGoBack: () -> Unit
}