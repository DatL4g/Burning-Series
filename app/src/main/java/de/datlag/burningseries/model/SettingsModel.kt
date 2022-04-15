package de.datlag.burningseries.model

import android.view.View
import android.widget.ImageView
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
        val listener: (switch: View, isChecked: Boolean) -> Unit
    ) : SettingsModel()

    data class Service(
        val key: Int,
        val title: String,
        val text: String = String(),
        var buttonText: String,
        var imageBind: (ImageView) -> Unit,
        val listener: () -> Unit
    ): SettingsModel()

    fun isSameItem(other: SettingsModel): Boolean = if (this is Group && other is Group) {
        this.key == other.key
    } else if (this is Switch && other is Switch) {
        this.key == other.key
    } else if (this is Service && other is Service) {
        this.key == other.key
    } else {
        false
    }

    fun isSameContent(other: SettingsModel): Boolean = if (this is Group && other is Group) {
        this.hashCode() == other.hashCode()
    } else if (this is Switch && other is Switch) {
        this.hashCode() == other.hashCode()
    } else if (this is Service && other is Service) {
        this.hashCode() == other.hashCode()
    } else {
        false
    }
}
