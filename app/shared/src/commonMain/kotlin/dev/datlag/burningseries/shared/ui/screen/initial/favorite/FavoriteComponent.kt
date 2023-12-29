package dev.datlag.burningseries.shared.ui.screen.initial.favorite

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.screen.initial.SeriesHolderComponent
import kotlinx.coroutines.flow.StateFlow

interface FavoriteComponent : SeriesHolderComponent {

    val favorites: StateFlow<List<Series>>
    val searchItems: StateFlow<List<Series>>

    val child: Value<ChildSlot<*, Component>>

    fun itemClicked(config: FavoriteConfig)
    fun searchQuery(text: String)
}