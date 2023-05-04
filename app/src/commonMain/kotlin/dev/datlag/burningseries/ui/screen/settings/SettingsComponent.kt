package dev.datlag.burningseries.ui.screen.settings

import dev.datlag.burningseries.database.DBHoster
import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow
import java.io.File
import dev.datlag.burningseries.model.ActionLogger

interface SettingsComponent : Component {

    val onGoBack: () -> Unit

    val hosterList: Flow<List<DBHoster>>
    val newRelease: Flow<Release?>

    val themeMode: Flow<Int>
    val amoled: Flow<Boolean>

    val errorFile: File
    val loggingMode: Flow<Int>
    val actionLogger: ActionLogger
    val loggingFile: File

    fun swapHoster(oldPos: Int, newPos: Int)

    fun changeThemeMode(state: Int)
    fun changeAmoledState(state: Boolean)

    fun changeLoggingMode(state: Int)
}