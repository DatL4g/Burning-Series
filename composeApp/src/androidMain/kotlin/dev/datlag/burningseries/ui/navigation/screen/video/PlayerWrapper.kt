package dev.datlag.burningseries.ui.navigation.screen.video

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.MediaSession2Service
import android.media.session.MediaSession.Token
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.type
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import coil3.annotation.InternalCoilApi
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import dev.datlag.burningseries.R
import dev.datlag.burningseries.ui.theme.SchemeTheme
import dev.datlag.nanoid.NanoIdUtils
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.compose.withMainContext
import dev.datlag.tooling.scopeCatching
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@UnstableApi
class PlayerWrapper(
    private val context: Context,
    castContext: CastContext?,
    private val startingPos: Long,
    private val startingLength: Long,
    private val headers: ImmutableMap<String, String>,
    private val coverData: ByteArray?,
    private val longTimeout: Boolean,
    private val onError: (PlaybackException) -> Unit = { },
    private val onFirstFrame: () -> Unit = { },
    private val onProgressChange: (Long) -> Unit = { },
    private val onLengthChange: (Long) -> Unit = { },
    private val onFinish: () -> Unit = { }
): SessionAvailabilityListener, Player.Listener {

    private val extractorFactory = DefaultExtractorsFactory().setTsExtractorFlags(
        FLAG_ALLOW_NON_IDR_KEYFRAMES and FLAG_DETECT_ACCESS_UNITS and FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS
    )

    private val dataSource: DataSource.Factory = DefaultHttpDataSource.Factory()
        .setDefaultRequestProperties(headers)
        .setAllowCrossProtocolRedirects(true)
        .setKeepPostFor302Redirects(true)
        .setReadTimeoutMs(
            if (longTimeout) {
                30.seconds.inWholeMilliseconds.toInt()
            } else {
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS
            }
        ).setConnectTimeoutMs(
            if (longTimeout) {
                30.seconds.inWholeMilliseconds.toInt()
            } else {
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS
            }
        )

    private val castPlayer = castContext?.let(::CastPlayer)

    private val localPlayer = ExoPlayer.Builder(context).apply {
        setSeekBackIncrementMs(10000)
        setSeekForwardIncrementMs(10000)
        setMediaSourceFactory(
            DefaultMediaSourceFactory(
                DefaultDataSource.Factory(context, dataSource),
                extractorFactory
            )
        )
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
            }
        }

    private var mediaItem: MediaItem? = player.currentMediaItem
        set(value) {
            val previous = field
            field = value

            if (previous != value && value != null) {
                player.setMediaItem(value.forPlayer(player), max(progress.value, startingPos))
                player.prepare()
                Session.createNew(context, player, value.mediaMetadata)
            }
        }

    private val _isPlaying = MutableStateFlow(player.isPlaying)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    val isCurrentlyPlaying: Boolean
        get() = isPlaying.value

    private val _progress = MutableStateFlow(startingPos)
    val progress: StateFlow<Long> = _progress

    private val _length = MutableStateFlow(max(max(progress.value, player.duration), startingLength))
    val length: StateFlow<Long> = _length

    private val _isFinished = MutableStateFlow(player.playbackState == Player.STATE_ENDED)
    val isFinished: StateFlow<Boolean> = _isFinished

    private val localPlayerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()

            // Playing works, can switch to casting
            castSupported = true
            onFirstFrame()
        }
    }

    private val _showControlsTime = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val showControls = _showControlsTime.transformLatest { time ->
        if (time > 0L) {
            emit(true)

            do {
                delay(3000)
                val newTime = Clock.System.now().toEpochMilliseconds()
                if (newTime - time >= 3000) {
                    _showControlsTime.update { 0L }
                }
            } while (currentCoroutineContext().isActive)
        } else {
            emit(false)
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
        _isFinished.update { player.playbackState == Player.STATE_ENDED }
        onProgressChange(_progress.updateAndGet { player.currentPosition })
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        super.onTimelineChanged(timeline, reason)

        onProgressChange(_progress.updateAndGet { player.currentPosition })
        _isFinished.update { player.playbackState == Player.STATE_ENDED }

        val newLength = player.duration
        if (newLength > 0) {
            _length.value = newLength
            onLengthChange(newLength)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        _isFinished.update { player.playbackState == Player.STATE_ENDED }
        if (playbackState == Player.STATE_ENDED) {
            onFinish()
        }
    }

    @OptIn(InternalCoilApi::class)
    fun play(mediaItem: MediaItem) {
        this.mediaItem = mediaItem
    }

    fun rewind() {
        player.seekBack()
        _showControlsTime.update { Clock.System.now().toEpochMilliseconds() }
    }

    fun forward() {
        player.seekForward()
        _showControlsTime.update { Clock.System.now().toEpochMilliseconds() }
    }

    fun togglePlay() {
        player.togglePlay()
        _showControlsTime.update { Clock.System.now().toEpochMilliseconds() }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        _showControlsTime.update { Clock.System.now().toEpochMilliseconds() }
    }

    suspend fun pollPosition() = withIOContext {
        do {
            withMainContext {
                onProgressChange(_progress.updateAndGet { player.currentPosition })
                _isFinished.update { player.playbackState == Player.STATE_ENDED }

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

    fun showControls() {
        _showControlsTime.update { Clock.System.now().toEpochMilliseconds() }
    }

    fun hideControls() {
        _showControlsTime.update { 0L }
    }

    fun showControlsFor5Min() {
        _showControlsTime.update { Clock.System.now().plus(5.minutes).toEpochMilliseconds() }
    }

    fun toggleControls() {
        _showControlsTime.update {
            if (it > 0L) {
                0L
            } else {
                Clock.System.now().toEpochMilliseconds()
            }
        }
    }

    fun dispatchKey(controlsVisible: Boolean, event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) {
            return false
        }

        val isDpadKey = event.key.isDpadKey()
        var handled = false

        if (isDpadKey && !controlsVisible) {
            // Handle the key event by showing the controller.
            showControls()
            handled = true
        } else if (dispatchMediaKey(event)) {
            showControls()
            handled = true
        } else if (isDpadKey) {
            // The key event wasn't handled, but we should extend the controller's show timeout.
            showControls()
        }
        return handled
    }

    private fun dispatchMediaKey(event: KeyEvent): Boolean {
        when {
            event.key.isSame(Key.MediaPlay) -> {
                player.play()
            }
            event.key.isSame(Key.MediaPause) -> {
                player.pause()
            }
            event.key.isSame(Key.MediaPlayPause) -> {
                player.togglePlay()
            }
            event.key.isSame(Key.MediaFastForward) -> {
                player.seekForward()
            }
            event.key.isSame(Key.MediaRewind) -> {
                player.seekBack()
            }
            event.key.isSame(Key.MediaNext) -> {
                if (length.value > 0L) {
                    player.seekTo(length.value)
                } else {
                    player.seekToNext()
                }
            }
            event.key.isSame(Key.MediaPrevious) -> {
                player.seekToPrevious()
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun Player.togglePlay() {
        if (this.isPlaying) {
            this.pause()
        } else {
            this.play()
        }
    }

    private fun MediaItem.forPlayer(player: Player): MediaItem {
        return if (player is CastPlayer) {
            this.buildUpon().setMimeType(MimeTypes.VIDEO_UNKNOWN).build()
        } else {
            this
        }
    }

    private fun Key.isDpadKey(): Boolean {
        return this.isSame(Key.DirectionUp)
                || this.isSame(Key.DirectionUpLeft)
                || this.isSame(Key.DirectionUpRight)
                || this.isSame(Key.DirectionRight)
                || this.isSame(Key.DirectionDown)
                || this.isSame(Key.DirectionDownLeft)
                || this.isSame(Key.DirectionDownRight)
                || this.isSame(Key.DirectionLeft)
                || this.isSame(Key.DirectionCenter)
    }

    private fun Key.isSame(key: Key): Boolean {
        return this == key || this.keyCode == key.keyCode || this.nativeKeyCode == key.nativeKeyCode
    }

    data object Session {
        private var current: MediaSession? = null
        private val PseudoRandom = Random((10000..12345).random())
        private val Alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val CHANNEL = "Video"

        fun createNew(context: Context, player: Player, metadata: MediaMetadata) {
            release()

            current = MediaSession.Builder(
                context,
                ForwardingPlayer(player)
            ).setId(
                NanoIdUtils.randomNanoId(
                    random = PseudoRandom,
                    alphabet = Alphabet.toCharArray()
                )
            ).build().also { session ->

                NotificationUtil.createNotificationChannel(
                    context,
                    CHANNEL,
                    R.string.session_title,
                    R.string.session_description,
                    NotificationUtil.IMPORTANCE_LOW
                )

                val notification = NotificationCompat.Builder(context, CHANNEL)
                    .setContentTitle(metadata.title)
                    .setContentText(metadata.subtitle?.ifBlank { null } ?: metadata.albumTitle?.ifBlank { null })
                    .setContentInfo(metadata.genre?.ifBlank { null })
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setStyle(MediaStyle(session))
                    .build()

                NotificationUtil.setNotification(context, 69, notification)
            }
        }

        fun release() {
            current?.release()
            current = null
        }
    }
}