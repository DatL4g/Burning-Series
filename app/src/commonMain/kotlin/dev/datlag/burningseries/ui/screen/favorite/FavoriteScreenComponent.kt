package dev.datlag.burningseries.ui.screen.favorite

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.model.SeriesInitialInfo
import kotlinx.coroutines.Dispatchers
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File

class FavoriteScreenComponent(
    componentContext: ComponentContext,
    override val onGoBack: () -> Unit,
    private val onSeries: (String, SeriesInitialInfo) -> Unit,
    override val di: DI
) : FavoriteComponent, ComponentContext by componentContext {

    private val db: BurningSeriesDB by di.instance()

    override val imageDir: File by di.instance("ImageDir")
    override val favorites = db.burningSeriesQueries.selectFavorites().asFlow().mapToList(Dispatchers.IO)


    override fun onSeriesClicked(href: String, initialInfo: SeriesInitialInfo) {
        onSeries(href, initialInfo)
    }

    @Composable
    override fun render() {
        FavoriteScreen(this)
    }
}