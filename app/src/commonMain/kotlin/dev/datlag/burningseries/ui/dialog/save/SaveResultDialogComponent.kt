package dev.datlag.burningseries.ui.dialog.save

import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.common.trimHref
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import org.kodein.di.DI

class SaveResultDialogComponent(
    componentContext: ComponentContext,
    override val series: Series,
    override val episode: Series.Episode,
    override val success: Boolean,
    override val stream: VideoStream?,
    override val scrapedEpisodeHref: String,
    private val onDismissed: () -> Unit,
    private val onWatch: (Series, Series.Episode, VideoStream) -> Unit,
    private val onBack: () -> Unit,
    override val di: DI
) : SaveResultComponent, ComponentContext by componentContext {

    override fun onDismissClicked() {
        onDismissed()
    }

    override fun watchClicked(stream: VideoStream) {
        val watchEpisode = series.episodes.find {
            it.href.trimHref().equals(scrapedEpisodeHref.trimHref(), true) || it.hoster.any { hoster ->
                hoster.href.trimHref().equals(scrapedEpisodeHref.trimHref(), true)
            }
        } ?: if (episode.href.trimHref().equals(scrapedEpisodeHref.trimHref(), true) || episode.hoster.any { hoster ->
            hoster.href.trimHref().equals(scrapedEpisodeHref.trimHref(), true)
        }) {
            episode
        } else {
            null
        }

        if (watchEpisode != null) {
            onWatch(series, watchEpisode, stream)
        }
    }

    override fun backClicked() {
        onBack()
    }
}