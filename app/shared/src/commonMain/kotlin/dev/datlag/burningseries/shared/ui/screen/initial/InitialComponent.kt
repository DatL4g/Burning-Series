package dev.datlag.burningseries.shared.ui.screen.initial

import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.StateFlow

interface InitialComponent : Component {

    val pagerItems: List<PagerItem>

    @OptIn(ExperimentalDecomposeApi::class)
    val pages: Value<ChildPages<*, Component>>

    val homeScrollEnabled: StateFlow<Boolean>
    val favoriteScrollEnabled: StateFlow<Boolean>
    val searchScrollEnabled: StateFlow<Boolean>

    fun selectPage(index: Int)

    data class PagerItem(
        val label: StringResource,
        val icon: ImageVector
    )
}