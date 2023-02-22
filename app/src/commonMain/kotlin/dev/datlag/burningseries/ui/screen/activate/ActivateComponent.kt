package dev.datlag.burningseries.ui.screen.activate

import com.arkivanov.decompose.router.overlay.ChildOverlay
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.SaveInfo
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.ui.dialog.DialogComponent
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient

interface ActivateComponent : Component {

    val dialog: Value<ChildOverlay<DialogConfig, DialogComponent>>

    val episode: Series.Episode
    val series: Series
    val onGoBack: () -> Unit

    val client: OkHttpClient

    val saveInfo: Flow<SaveInfo>

    val status: MutableStateFlow<Status>

    fun saveScrapedData(result: String)
    fun saveScrapedData(href: String, url: String)
    fun setStatus(status: Status)
}