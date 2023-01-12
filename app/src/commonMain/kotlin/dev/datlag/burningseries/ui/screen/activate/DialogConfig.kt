package dev.datlag.burningseries.ui.screen.activate

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.VideoStream

@Parcelize
sealed class DialogConfig : Parcelable {

    data class SaveResult(
        val success: Boolean,
        val stream: VideoStream?,
        val scrapedEpisodeHref: String
    ) : DialogConfig()
}
