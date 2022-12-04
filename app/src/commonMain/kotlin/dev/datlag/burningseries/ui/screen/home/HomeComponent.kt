package dev.datlag.burningseries.ui.screen.home

import com.arkivanov.decompose.router.overlay.ChildOverlay
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.network.repository.HomeRepository
import dev.datlag.burningseries.ui.dialog.DialogComponent
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow

interface HomeComponent : Component {

    val onSeriesClicked: (String, SeriesInitialInfo) -> Unit

    val dialog: Value<ChildOverlay<*, DialogComponent>>

    val childStack: Value<ChildStack<*, Component>>
    val childIndex: MutableValue<Int>

    fun showDialog(message: String)
    fun onSearchClicked()

}