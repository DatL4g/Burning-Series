package dev.datlag.burningseries.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.Track
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory.*
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import dev.datlag.burningseries.R
import dev.datlag.burningseries.common.findActivity
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.mutable
import dev.datlag.burningseries.common.safeEmit
import dev.datlag.burningseries.model.Language
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.other.Logger
import dev.datlag.burningseries.ui.activity.KeyEventDispatcher
import dev.datlag.burningseries.ui.activity.PIPEventDispatcher
import dev.datlag.burningseries.ui.activity.PIPModeListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Locale

@SuppressLint("ViewConstructor")
@UnstableApi
class ExtendedPlayer private constructor(
    castContext: CastContext?,
    context: Context,
    private val scope: CoroutineScope,
    private val initialStreams: StateFlow<List<VideoStream>>,
    private val position: StateFlow<Long>,
    private val progress: (Long) -> Unit,
    private val ended: () -> Unit = { }
) : FrameLayout(context), CustomPlayer, Player.Listener, SessionAvailabilityListener {

    private val currentStreams: MutableStateFlow<List<VideoStream>> = initialStreams.mutable(scope)

    private val currentStreamIndex: MutableStateFlow<Int> = currentStreams.mapLatest { 0 }.mutable(0, scope)
    private val currentSourceIndex: MutableStateFlow<Int> = currentStreams.mapLatest { 0 }.mutable(0, scope)

    private var streamHeader = combine(currentStreams, currentStreamIndex) { list, index ->
        list[index].header
    }.stateIn(scope, SharingStarted.WhileSubscribed(), currentStreams.value[currentStreamIndex.value].header)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dataSource = streamHeader.mapLatest {
        buildDataSource(context, it)
    }.stateIn(scope, SharingStarted.WhileSubscribed(), buildDataSource(context, streamHeader.value))

    private val extractorFactory = DefaultExtractorsFactory().setTsExtractorFlags(
        FLAG_ALLOW_NON_IDR_KEYFRAMES and FLAG_DETECT_ACCESS_UNITS and FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val localPlayer = dataSource.mapLatest {
        try {
            val prev = player
            if (prev is ExoPlayer) {
                prev.release()
            }
        } catch (ignored: Throwable) { }
        withContext(Dispatchers.Main) {
            buildLocalPlayer(context, it)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(), null)

    private val castPlayer = castContext?.let {
        CastPlayer(it).apply {
            addListener(this@ExtendedPlayer)
            setSessionAvailabilityListener(this@ExtendedPlayer)
            playWhenReady = true
        }
    }

    private val playerOption: MutableStateFlow<PlayerOptions> = MutableStateFlow(PlayerOptions.UNDEFINED)

    private val currentPlayer = combine(localPlayer.mapNotNull { it }, playerOption) { player, option ->
        when (option) {
            PlayerOptions.UNDEFINED -> {
                if (castPlayer?.isCastSessionAvailable == true && streamHeader.value.isEmpty()) {
                    castPlayer
                } else {
                    player
                }
            }
            PlayerOptions.LOCAL -> player
            PlayerOptions.CAST -> {
                castPlayer ?: player
            }
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(), run {
        if (castPlayer?.isCastSessionAvailable == true && streamHeader.value.isEmpty()) {
            castPlayer
        } else {
            localPlayer.getValueBlocking(null)
        }
    })

    private val currentMediaItemURI = combine(currentStreams, currentStreamIndex, currentSourceIndex) { list, stream, source ->
        list[stream].srcList[source]
    }.stateIn(scope, SharingStarted.WhileSubscribed(), currentStreams.value[currentStreamIndex.value].srcList[currentSourceIndex.value])

    private val player: Player?
        get() = currentPlayer.value

    private val playerFlow: Flow<Player> = currentPlayer.mapNotNull { it }

    private val mediaItemURI: String
        get() = currentMediaItemURI.value

    private val mediaItemUriFlow: Flow<String> = currentMediaItemURI

    private var session: MediaSession? = null

    val subtitles: MutableStateFlow<List<Language>> = MutableStateFlow(emptyList())
    val selectedLanguage: MutableStateFlow<Language?> = MutableStateFlow(null)

    var castAvailableJob: Job? = null

    init {
        setBackgroundColor(Color.BLACK)
        inflate(context, R.layout.video_player, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        keepScreenOn = true

        val playerView = findViewById<PlayerView>(R.id.player)
        val controls = playerView.findViewById<View>(R.id.exoplayer_controls)
        val mediaRouteButton = findViewById<MediaRouteButton>(R.id.castButton)

        CastButtonFactory.setUpMediaRouteButton(
            context.findActivity() ?: context,
            mediaRouteButton
        )

        playerView.player = player?.apply {
            setMediaItem(MediaItem.fromUri(mediaItemURI), position.value)
            prepare()
        }

        KeyEventDispatcher = { event ->
            event?.let { ev -> playerView.dispatchKeyEvent(ev) }
        }
        PIPEventDispatcher = {
            true
        }
        PIPModeListener = { isInPIP ->
            if (isInPIP) {
                controls.visibility = GONE
            } else {
                controls.visibility = VISIBLE
            }
        }

        scope.launch(Dispatchers.IO) {
            combine(playerFlow, mediaItemUriFlow) { p, m ->
                p to m
            }.collect { (p, m) ->
                withContext(Dispatchers.Main) {
                    val prevPlay = playerView.player
                    var position = this@ExtendedPlayer.position.value
                    var playWhenReady = true

                    if (p != prevPlay) {
                        val state = prevPlay?.playbackState
                        if (state != Player.STATE_ENDED) {
                            position = prevPlay?.currentPosition ?: position
                            playWhenReady = prevPlay?.playWhenReady ?: playWhenReady
                        }
                        prevPlay?.stop()
                        prevPlay?.clearMediaItems()
                    }
                    playerView.player = p.apply {
                        setMediaItem(MediaItem.fromUri(m), position)
                        this.playWhenReady = playWhenReady
                        prepare()
                    }

                    session?.release()
                    session = MediaSession.Builder(context, p).build()
                }
            }
        }
        scope.launch(Dispatchers.IO) {
            combine(subtitles, selectedLanguage) { l, s ->
                l to s
            }.collect { (languages, selected) ->
                val chosen = languages.firstOrNull { it.code == selected?.code }
                withContext(Dispatchers.Main) {
                    player?.let { p ->
                        p.trackSelectionParameters = p.trackSelectionParameters
                            .buildUpon()
                            .setPreferredTextLanguage(chosen?.code)
                            .build()
                    }
                }
            }
        }
        scope.launch(Dispatchers.IO) {
            streamHeader.map { it.isEmpty() }.collect { supported ->
                withContext(Dispatchers.Main) {
                    controlsView.findViewById<MediaRouteButton>(R.id.castButton).isEnabled = supported
                }
            }
        }
    }

    private fun buildDataSource(context: Context, header: Map<String, String>): DataSource.Factory {
        return DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory().setDefaultRequestProperties(header)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true))
    }

    private fun buildLocalPlayer(context: Context, dataSource: DataSource.Factory): ExoPlayer {
        return ExoPlayer.Builder(context).apply {
            setSeekBackIncrementMs(10000)
            setSeekForwardIncrementMs(10000)
            setPauseAtEndOfMediaItems(true)
            setMediaSourceFactory(DefaultMediaSourceFactory(dataSource, extractorFactory))
        }.build().apply {
            addListener(this@ExtendedPlayer)
            playWhenReady = true
        }
    }

    override val isLoading: Boolean
        get() = with(player) {
            this?.isLoading ?: currentPlayer.value?.isLoading ?: true
        }
    override val isPlaying: Boolean
        get() = with(player) {
            this?.isPlaying ?: currentPlayer.value?.isPlaying ?: false
        }

    val isCasting: Boolean
        get() = player is CastPlayer

    override fun play() {
        player?.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun seekForward() {
        player?.seekForward()
    }

    override fun seekBack() {
        player?.seekBack()
    }

    override fun seekTo(millis: Long) {
        player?.seekTo(millis)
    }



    override fun onCastSessionAvailable() {
        castAvailableJob?.cancel()
        castAvailableJob = scope.launch(Dispatchers.IO) {
            streamHeader.map { it.isEmpty() }.collect { supported ->
                if (supported) {
                    playerOption.emit(PlayerOptions.CAST)
                }
            }
        }
    }

    override fun onCastSessionUnavailable() {
        castAvailableJob?.cancel()
        playerOption.safeEmit(PlayerOptions.LOCAL, scope)
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)

        scope.launch(Dispatchers.IO) {
            if (currentStreams.value[currentStreamIndex.value].srcList.size - 1 > currentSourceIndex.value) {
                withContext(Dispatchers.Main) {
                    currentSourceIndex.emit(currentSourceIndex.value + 1)
                }
            } else {
                if (currentStreams.value.size - 1 > currentStreamIndex.value) {
                    withContext(Dispatchers.Main) {
                        currentStreamIndex.emit(currentStreamIndex.value + 1)
                        currentSourceIndex.emit(0)
                    }
                }
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        if (playbackState == Player.STATE_ENDED) {
            ended.invoke()
        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        super.onTracksChanged(tracks)

        val languages = tracks.groups.mapNotNull { group ->
            if (group.type != C.TRACK_TYPE_TEXT) {
                return@mapNotNull null
            }

            val formats = (0 until group.length).map { index ->
                group.getTrackFormat(index)
            }
            formats.mapNotNull { format ->
                format.language
            }
        }.flatten().toSet().map {
            Language(
                code = it,
                title = Locale(it).displayLanguage
            )
        }
        subtitles.safeEmit(languages, scope)
    }

    override fun onRenderedFirstFrame() {
        super.onRenderedFirstFrame()

        scope.launch(Dispatchers.IO) {
            delay(3000)
            while (this.isActive) {
                withContext(Dispatchers.Main) {
                    player?.currentPosition?.let {
                        progress.invoke(it)
                    }
                }
                delay(500)
            }
        }
    }



    fun release() {
        playerView.player = null
        castPlayer?.setSessionAvailabilityListener(null)
        localPlayer.value?.release()
        castPlayer?.release()
        session?.release()
    }

    fun setPreferredLanguage(language: Language?) {
        selectedLanguage.safeEmit(language, scope)
    }

    val playerView: PlayerView
        get() = findViewById(R.id.player)

    val controlsView: View
        get() = playerView.findViewById(R.id.exoplayer_controls)

    sealed interface PlayerOptions {
        object UNDEFINED : PlayerOptions
        object LOCAL : PlayerOptions
        object CAST : PlayerOptions
    }

    class Builder(private val context: Context, private val scope: CoroutineScope) {

        private var _streamFlow: StateFlow<List<VideoStream>> = MutableStateFlow(emptyList())
        private var _castContext: CastContext? = CastContext.getSharedInstance()
        private var _position: StateFlow<Long> = MutableStateFlow(0)
        private var _progress: (Long) -> Unit = { }
        private var _ended: () -> Unit = { }

        fun castContext(castContext: CastContext?) = apply {
            _castContext = castContext
        }

        fun position(flow: StateFlow<Long>) = apply {
            _position = flow
        }

        fun streamFlow(flow: StateFlow<List<VideoStream>>) = apply {
            _streamFlow = flow
        }

        fun streamFlow(flow: Flow<List<VideoStream>>, initValue: List<VideoStream>) = apply {
            _streamFlow = flow.stateIn(scope, SharingStarted.WhileSubscribed(), initValue)
        }

        fun onProgress(listener: (Long) -> Unit) = apply {
            _progress = listener
        }

        fun onEnded(`do`: () -> Unit) {
            _ended = `do`
        }

        fun build() = ExtendedPlayer(
            _castContext,
            context,
            scope,
            _streamFlow,
            _position,
            _progress,
            _ended
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun Context.extendedPlayer(
    scope: CoroutineScope,
    builder: ExtendedPlayer.Builder.() -> Unit
) = ExtendedPlayer.Builder(this, scope).apply(builder).build()