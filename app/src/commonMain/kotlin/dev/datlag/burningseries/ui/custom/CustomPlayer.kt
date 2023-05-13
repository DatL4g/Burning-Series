package dev.datlag.burningseries.ui.custom

interface CustomPlayer {

    val isLoading: Boolean
    val isPlaying: Boolean
    val isPaused: Boolean
        get() = !isPlaying && !isLoading

    fun play()
    fun pause()
    fun triggerPlay() {
        if (isPlaying) {
            pause()
        } else if (isPaused) {
            play()
        }
    }
    fun seekForward()
    fun seekBack()
    fun seekTo(millis: Long)
}