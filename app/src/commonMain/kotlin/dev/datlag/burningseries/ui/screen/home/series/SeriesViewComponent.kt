package dev.datlag.burningseries.ui.screen.home.series

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.datastore.preferences.UserSettings
import dev.datlag.burningseries.network.repository.HomeRepository
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SeriesViewComponent(
    componentContext: ComponentContext,
    override val di: DI
) : SeriesComponent, ComponentContext by componentContext {

    private val homeRepo: HomeRepository by di.instance()
    override val status = homeRepo.status
    override val series = homeRepo.series

    @Composable
    override fun render() {
        val userDataStore: DataStore<UserSettings> by di.instance()
        val currentSettings = userDataStore.data.collectAsState(UserSettings.getDefaultInstance())

        Column {
            Text("User name: ${currentSettings.value.burningSeries.username}")
            Text("Password: ${currentSettings.value.burningSeries.password}")
        }
    }
}