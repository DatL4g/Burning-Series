package dev.datlag.burningseries.ui.navigation.screen.home.dialog.release

import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.burningseries.ui.navigation.DialogComponent

interface ReleaseComponent : DialogComponent {
    val release: UserAndRelease.Release
}