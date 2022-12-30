package dev.datlag.burningseries.ui.screen.settings

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.database.DBHoster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.instance
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.model.common.move
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.network.repository.GitHubRepository
import dev.datlag.burningseries.other.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsScreenComponent(
    componentContext: ComponentContext,
    override val onGoBack: () -> Unit,
    override val di: DI
) : SettingsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val db: BurningSeriesDB by di.instance()
    private val githubRepo: GitHubRepository by di.instance()

    override val newRelease: Flow<Release?> = githubRepo.newRelease

    override val hosterList: Flow<List<DBHoster>> = db.burningSeriesQueries.selectAllHosters().asFlow().mapToList(Dispatchers.IO).map {
        it.sortedBy { hoster ->
            hoster.position ?: Int.MAX_VALUE
        }
    }

    override fun swapHoster(oldPos: Int, newPos: Int) {
        scope.launch(Dispatchers.IO) {
            val list = hosterList.first().toMutableList()
            list.move(oldPos, newPos).forEachIndexed { index, item ->
                db.burningSeriesQueries.updateHosterPosition(index, item.name)
            }
        }
    }

    @Composable
    override fun render() {
        SettingsScreen(this)
    }
}