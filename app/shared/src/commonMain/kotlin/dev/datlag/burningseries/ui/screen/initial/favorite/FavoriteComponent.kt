package dev.datlag.burningseries.ui.screen.initial.favorite

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface FavoriteComponent : Component {

    val favorites: StateFlow<List<Series>>
    val searchItems: StateFlow<List<Series>>

    val child: Value<ChildSlot<*, Component>>

    fun itemClicked(config: FavoriteConfig)
    fun searchQuery(text: String)
}