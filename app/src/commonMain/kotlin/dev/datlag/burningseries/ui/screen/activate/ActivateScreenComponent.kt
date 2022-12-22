package dev.datlag.burningseries.ui.screen.activate

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.model.ScrapedHoster
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.repository.SaveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.instance

class ActivateScreenComponent(
    componentContext: ComponentContext,
    override val series: Series,
    override val episode: Series.Episode,
    override val onGoBack: () -> Unit,
    override val di: DI
) : ActivateComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val json: Json by di.instance()
    private val saveRepo: SaveRepository by di.instance()
    override val client: OkHttpClient by di.instance()
    override val saveSuccess: Flow<Boolean> = saveRepo.saveScrapedSuccess

    private val scrapedList: MutableList<ScrapedHoster> = mutableListOf()

    override fun saveScrapedData(result: String) {
        val scraped = json.decodeFromString<ScrapedHoster>(result)
        if (!scrapedList.contains(scraped)) {
            scrapedList.add(scraped)
            scope.launch(Dispatchers.IO) {
                saveRepo.save(scraped)
            }
        }
    }

    @Composable
    override fun render() {
        ActivateScreen(this)
    }
}