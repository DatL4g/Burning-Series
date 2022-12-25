package dev.datlag.burningseries.ui.screen.about

import dev.datlag.burningseries.ui.navigation.Component

interface AboutComponent : Component {

    val onGoBack: () -> Unit
}