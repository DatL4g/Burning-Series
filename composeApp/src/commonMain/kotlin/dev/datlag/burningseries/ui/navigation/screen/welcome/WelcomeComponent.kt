package dev.datlag.burningseries.ui.navigation.screen.welcome

import dev.datlag.burningseries.settings.model.Language
import dev.datlag.burningseries.ui.navigation.Component

interface WelcomeComponent : Component {

    fun start(language: Language)
}