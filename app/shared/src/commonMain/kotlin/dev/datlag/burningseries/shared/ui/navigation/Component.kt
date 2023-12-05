package dev.datlag.burningseries.shared.ui.navigation

import androidx.compose.runtime.Composable
import org.kodein.di.DIAware

interface Component : DIAware {

    @Composable
    fun render()
}