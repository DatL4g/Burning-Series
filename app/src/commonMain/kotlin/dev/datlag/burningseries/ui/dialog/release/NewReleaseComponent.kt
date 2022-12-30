package dev.datlag.burningseries.ui.dialog.release

import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.ui.dialog.DialogComponent
import kotlinx.coroutines.flow.Flow

interface NewReleaseComponent : DialogComponent {

    val newRelease: Release
}