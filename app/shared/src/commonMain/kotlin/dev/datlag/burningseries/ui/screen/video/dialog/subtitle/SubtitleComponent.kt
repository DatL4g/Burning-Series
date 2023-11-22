package dev.datlag.burningseries.ui.screen.video.dialog.subtitle

import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.burningseries.ui.screen.video.VideoComponent

interface SubtitleComponent : DialogComponent {

    val initialChosen: VideoComponent.Subtitle?
    val list: List<VideoComponent.Subtitle>

    fun choose(target: VideoComponent.Subtitle?)
}