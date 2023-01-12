package dev.datlag.burningseries.ui.dialog.save

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.ui.dialog.DialogComponent

interface SaveResultComponent : DialogComponent {

    val series: Series
    val episode: Series.Episode

    val success: Boolean
    val stream: VideoStream?
    val scrapedEpisodeHref: String

    fun watchClicked(stream: VideoStream)
    fun backClicked()
}