package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import java.awt.Component

interface MediaPlayer {
    val component: Component
    val isPlaying: MutableState<Boolean>
    val time: MutableLongState
    val length: MutableLongState
    val isMuted: MutableState<Boolean>
    val volume: MutableFloatState
    fun play()
    fun pause()
    fun rewind()
    fun forward()
    fun seekTo(millis: Long)
    fun mute()
    fun unmute()
    fun setVolume(volume: Float)
    fun startPlaying()
}