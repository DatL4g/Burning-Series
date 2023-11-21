package dev.datlag.burningseries.ui.screen.video

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState

interface MediaPlayer {
    val isPlaying: MutableState<Boolean>
    val time: MutableLongState
    val length: MutableLongState
    fun play()
    fun pause()
    fun rewind()
    fun forward()
    fun seekTo(millis: Long)
}