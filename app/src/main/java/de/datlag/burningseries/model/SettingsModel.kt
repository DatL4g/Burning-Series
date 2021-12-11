package de.datlag.burningseries.model

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
sealed class SettingsModel {
    data class Group(val title: String) : SettingsModel()
    data class Switch(
        val title: String,
        val text: String = String(),
        var defaultValue: Boolean = false,
        val listener: (isChecked: Boolean) -> Unit
    ) : SettingsModel()
}
