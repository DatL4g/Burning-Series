package dev.datlag.burningseries.ui.screen.activate

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient

interface ActivateComponent : Component {

    val episode: Series.Episode
    val series: Series
    val onGoBack: () -> Unit

    val client: OkHttpClient
    val saveSuccess: Flow<Boolean>

    fun saveScrapedData(result: String)
}