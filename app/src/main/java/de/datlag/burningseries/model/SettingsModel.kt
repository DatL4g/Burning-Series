package de.datlag.burningseries.model

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
sealed class SettingsModel {
    data class Group(val key: Int, val title: String) : SettingsModel()
    data class Switch(
        val key: Int,
        val title: String,
        val text: String = String(),
        var defaultValue: Boolean = false,
        var enabled: Boolean = true,
        val listener: (isChecked: Boolean) -> Unit
    ) : SettingsModel()

    fun isSameItem(other: SettingsModel): Boolean = if (this is Group && other is Group) {
        this.key == other.key
    } else if (this is Switch && other is Switch) {
        this.key == other.key
    } else {
        false
    }

    fun isSameContent(other: SettingsModel): Boolean = if (this is Group && other is Group) {
        this.hashCode() == other.hashCode()
    } else if (this is Switch && other is Switch) {
        this.hashCode() == other.hashCode()
    } else {
        false
    }
}
