package dev.datlag.burningseries.ui.dialog

import dev.datlag.burningseries.ui.navigation.Component

interface DialogComponent {

    val message: String

    fun onDismissClicked()
}