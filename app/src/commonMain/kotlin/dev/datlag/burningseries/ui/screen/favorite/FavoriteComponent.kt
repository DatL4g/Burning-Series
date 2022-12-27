package dev.datlag.burningseries.ui.screen.favorite

import dev.datlag.burningseries.database.DBSeries
import dev.datlag.burningseries.ui.screen.SeriesItemComponent
import kotlinx.coroutines.flow.Flow

interface FavoriteComponent : SeriesItemComponent {

    val onGoBack: () -> Unit

    val favorites: Flow<List<DBSeries>>
}