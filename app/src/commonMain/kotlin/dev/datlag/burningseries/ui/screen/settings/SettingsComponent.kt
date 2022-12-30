package dev.datlag.burningseries.ui.screen.settings

import dev.datlag.burningseries.database.DBHoster
import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow

interface SettingsComponent : Component {

    val onGoBack: () -> Unit

    val hosterList: Flow<List<DBHoster>>
    val newRelease: Flow<Release?>

    fun swapHoster(oldPos: Int, newPos: Int)
}