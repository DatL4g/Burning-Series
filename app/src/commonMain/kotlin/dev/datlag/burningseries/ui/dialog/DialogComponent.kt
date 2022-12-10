package dev.datlag.burningseries.ui.dialog

import dev.datlag.burningseries.ui.navigation.Component
import org.kodein.di.DIAware

interface DialogComponent : DIAware {

    fun onDismissClicked()
}