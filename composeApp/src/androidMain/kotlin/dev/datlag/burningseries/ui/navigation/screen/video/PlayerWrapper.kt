package dev.datlag.burningseries.ui.navigation.screen.video

import android.content.Context
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import coil3.annotation.InternalCoilApi
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import dev.datlag.nanoid.NanoIdUtils
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.compose.withMainContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.random.Random

@UnstableApi
class PlayerWrapper(
    private val context: Context,
    castContext: CastContext?,
    private val startingPos: Long,
    private val startingLength: Long,
    private val onError: (PlaybackException) -> Unit = { },
    private val onProgressChange: (Long) -> Unit = { },
    private val onLengthChange: (Long) -> Unit = { },
): SessionAvailabilityListener, Player.Listener {

    private val castPlayer = castContext?.let(::CastPlayer)

    private val localPlayer = ExoPlayer.Builder(context).apply {
        setSeekBackIncrementMs(10000)
        setSeekForwardIncrementMs(10000)
    }.build()

    private val castState = castContext?.castState
    private val casting = castState == CastState.CONNECTED
    private var castSupported = false
        set(value) {
            field = value

            if (value && requestCasting) {
                useCastPlayer = true
            }
        }
    private var requestCasting = casting
        set(value) {
            field = value

            if (castSupported && value) {
                useCastPlayer = true
            }
        }
    private var useCastPlayer = castSupported && requestCasting
        set(value) {
            val previous = field
            field = value

            if (value != previous) {
                player = if (value) {
                    castPlayer.also {
                        // ToDo("mute and ignore state change instead? would display video with no sound")
                        localPlayer.pause()
                    } ?: localPlayer
                } else {
                    localPlayer
                }
            }
        }

    var player: Player = if (useCastPlayer) castPlayer ?: localPlayer else localPlayer
        private set(value) {
            val previous = field
            field = value

            if (previous != value) {
                mediaItem?.let {
                    value.setMediaItem(it.forPlayer(value), max(progress.value, startingPos))
                    value.prepare()
                }

                Session.createNew(context, value)
            }
        }

    private var mediaItem: MediaItem? = player.currentMediaItem
        set(value) {
            val previous = field
            field = value

            if (previous != value && value != null) {
                player.setMediaItem(value.forPlayer(player), max(progress.value, startingPos))
                player.prepare()
            }
        }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(startingPos)
    val progress: StateFlow<Long> = _progress

    private val _length = MutableStateFlow(max(max(progress.value, player.duration), startingLength))
    val length: StateFlow<Long> = _length

    private val localPlayerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()

            // Playing works, can switch to casting
            castSupported = true
        }
    }

    init {
        castPlayer?.addListener(this)
        localPlayer.addListener(localPlayerListener)
        localPlayer.addListener(this)

        castPlayer?.setSessionAvailabilityListener(this)

        castPlayer?.playWhenReady = true
        localPlayer.playWhenReady = true
    }

    override fun onCastSessionAvailable() {
        requestCasting = true
    }

    override fun onCastSessionUnavailable() {
        requestCasting = false
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)

        onError(error)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)

        _isPlaying.update { isPlaying }

        onProgressChange(_progress.updateAndGet { player.currentPosition })
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        super.onTimelineChanged(timeline, reason)

        onProgressChange(_progress.updateAndGet { player.currentPosition })

        val newLength = player.duration
        if (newLength > 0) {
            _length.value = newLength
            onLengthChange(newLength)
        }
    }

    @OptIn(InternalCoilApi::class)
    fun play(mediaItem: MediaItem) {
        this.mediaItem = mediaItem
    }

    fun rewind() {
        player.seekBack()
    }

    fun forward() {
        player.seekForward()
    }

    fun togglePlay() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    suspend fun pollPosition() = withIOContext {
        do {
            withMainContext {
                onProgressChange(_progress.updateAndGet { player.currentPosition })

                val newLength = player.duration
                if (newLength > 0 && _length.value != newLength) {
                    onLengthChange(_length.updateAndGet { newLength })
                }
            }
            delay(100)
        } while (currentCoroutineContext().isActive)
    }

    fun release() {
        localPlayer.removeListener(this)
        localPlayer.removeListener(localPlayerListener)
        castPlayer?.removeListener(this)
        castPlayer?.setSessionAvailabilityListener(null)

        localPlayer.release()
        castPlayer?.stop()
        castPlayer?.clearMediaItems()
        Session.release()
    }

    private fun MediaItem.forPlayer(player: Player): MediaItem {
        return if (player is CastPlayer) {
            this.buildUpon().setMimeType(MimeTypes.VIDEO_UNKNOWN).build()
        } else {
            this
        }
    }

    data object Session {
        private var current: MediaSession? = null
        private val PseudoRandom = Random((10000..12345).random())
        private val Alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        fun createNew(context: Context, player: Player) {
            release()

            current = MediaSession.Builder(
                context,
                (player as? ForwardingPlayer) ?: ForwardingPlayer(player)
            ).setId(
                NanoIdUtils.randomNanoId(
                    random = PseudoRandom,
                    alphabet = Alphabet.toCharArray()
                )
            ).build()
        }

        fun release() {
            current?.release()
            current = null
        }
    }
}