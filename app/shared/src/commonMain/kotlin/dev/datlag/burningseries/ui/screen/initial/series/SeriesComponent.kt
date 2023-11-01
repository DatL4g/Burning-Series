package dev.datlag.burningseries.ui.screen.initial.series

import dev.datlag.burningseries.ui.navigation.Component

interface SeriesComponent : Component {

    val initialTitle: String
    val initialCoverHref: String?
}