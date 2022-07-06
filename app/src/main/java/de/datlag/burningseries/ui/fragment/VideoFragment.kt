package de.datlag.burningseries.ui.fragment

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.KeyEvent
import android.view.TextureView
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.rubensousa.previewseekbar.PreviewLoader
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentVideoBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.helper.lazyMutable
import de.datlag.burningseries.ui.connector.BackPressedDispatcher
import de.datlag.burningseries.ui.connector.KeyEventDispatcher
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.burningseries.viewmodel.VideoViewModel
import de.datlag.coilifier.ImageLoader
import de.datlag.database.burningseries.BurningSeriesDao
import de.datlag.executor.Executor
import de.datlag.executor.Schema
import de.datlag.model.burningseries.series.EpisodeInfo
import de.datlag.model.burningseries.series.relation.EpisodeWithHoster
import de.datlag.model.burningseries.stream.Stream
import de.datlag.model.common.getBestConfig
import de.datlag.model.video.ScrapeHoster
import de.datlag.model.video.VideoStream
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wseemann.media.FFmpegMediaMetadataRetriever
import javax.inject.Inject

@AndroidEntryPoint
@Obfuscate
class VideoFragment : AdvancedFragment(R.layout.fragment_video), PreviewLoader, Player.Listener, KeyEventDispatcher, BackPressedDispatcher {

    private val navArgs: VideoFragmentArgs by navArgs()
    private val binding: FragmentVideoBinding by viewBinding()

    private val videoViewModel: VideoViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var retriever: FFmpegMediaMetadataRetriever

    private val positionState: MutableStateFlow<Long> = MutableStateFlow(0)
    private val playingState: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private var episodeInfo: EpisodeInfo by lazyMutable { navArgs.episodeInfo }

    private var nextEpisodeInfo: EpisodeWithHoster? = null
    private var nextEpisodePlainStreams: MutableSet<Stream> = mutableSetOf()
    private var nextEpisodeStreams: MutableSet<VideoStream> = mutableSetOf()


    @Inject
    lateinit var executor: Executor

    @Inject
    lateinit var saveExecutor: Executor

    @Inject
    lateinit var burningSeriesDao: BurningSeriesDao

    private var framePosStep: Long = 0L

    private var showingSkipType: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findActualEpisodeInfo()
        initPlayer()
    }

    private fun findActualEpisodeInfo(
        id: Long = navArgs.episodeInfo.episodeId,
        href: String = navArgs.episodeInfo.href
    ) = burningSeriesDao.getEpisodeInfoByIdOrHref(id, href).distinctUntilChanged().launchAndCollect {
        episodeInfo = it ?: episodeInfo
    }

    private fun initPlayer(): Unit = with(binding) {
        retriever = FFmpegMediaMetadataRetriever()

        val extractorFactory = DefaultExtractorsFactory().setTsExtractorFlags(FLAG_DETECT_ACCESS_UNITS)
        if (!::exoPlayer.isInitialized) {
            exoPlayer = ExoPlayer.Builder(safeContext).apply {
                setSeekBackIncrementMs(10000)
                setSeekForwardIncrementMs(10000)
                setPauseAtEndOfMediaItems(true)
                setMediaSourceFactory(DefaultMediaSourceFactory(safeContext, extractorFactory))
            }.build().apply {
                addListener(this@VideoFragment)
                playWhenReady = true
            }
        }
        player.player = exoPlayer

        player.setTitle(episodeInfo.title)
        player.setFullscreenListener {
            val controllerCompat = safeActivity?.window?.let { controller -> return@let WindowInsetsControllerCompat(controller, controller.decorView) }
            if (it) {
                controllerCompat?.hide(WindowInsetsCompat.Type.systemBars())
                controllerCompat?.hide(WindowInsetsCompat.Type.ime())
                controllerCompat?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                safeActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                controllerCompat?.show(WindowInsetsCompat.Type.systemBars())
                controllerCompat?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
                safeActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        player.setPreviewLoader(this@VideoFragment)
        player.setPreviewImage(ImageLoader.create(getVideoFrame(-1L)))

        player.setOnBackPressed {
            onBackPressed()
        }
        player.setConfigSaveListener { saveConfig, isNewEntry ->
            val saveStream = ScrapeHoster(navArgs.href, navArgs.videoStream.defaultUrl, saveConfig)
            val success = burningSeriesViewModel.patchStream(saveStream).first()
            if (success) {
                settingsViewModel.increaseUsageTimeEditAmount(isNewEntry)
            }
            success
        }

        createStreamConfigListener()

        listenSettingsState()
        listenVideoSourceState()
        listenPositionState()
        listenPlayingState()
    }

    private fun createStreamConfigListener() = with(binding) {
        player.config = navArgs.streamConfig
        val throwBackStart = navArgs.streamConfig.throwback.start
        val throwBackEnd = navArgs.streamConfig.throwback.end
        val introStart = navArgs.streamConfig.intro.start
        val introEnd = navArgs.streamConfig.intro.end
        val outroStart = navArgs.streamConfig.outro.start
        val outroEnd = navArgs.streamConfig.outro.end

        if (throwBackStart != null && throwBackEnd != null) {
            val throwBackStartMessage = exoPlayer.createMessage { _, _ ->
                showSkip(0, throwBackEnd)
            }
            val throwBackEndMessage = exoPlayer.createMessage { _, _ ->
                hideSkip(0)
            }

            throwBackStartMessage.setPosition(throwBackStart)
                .setDeleteAfterDelivery(false)
                .send()
            throwBackEndMessage.setPosition(throwBackEnd)
                .setDeleteAfterDelivery(false)
                .send()
        }

        if (introStart != null && introEnd != null) {
            val introStartMessage = exoPlayer.createMessage { _, _ ->
                showSkip(1, introEnd)
            }
            val introEndMessage = exoPlayer.createMessage { _, _ ->
                hideSkip(1)
            }

            introStartMessage.setPosition(introStart)
                .setDeleteAfterDelivery(false)
                .send()
            introEndMessage.setPosition(introEnd)
                .setDeleteAfterDelivery(false)
                .send()
        }

        if (outroStart != null && outroEnd != null) {
            val outroStartMessage = exoPlayer.createMessage { _, _ ->
                showSkip(2, outroEnd)
            }
            val outroEndMessage = exoPlayer.createMessage { _, _ ->
                hideSkip(2)
            }

            outroStartMessage.setPosition(outroStart)
                .setDeleteAfterDelivery(false)
                .send()
            outroEndMessage.setPosition(outroEnd)
                .setDeleteAfterDelivery(false)
                .send()
        }
    }

    private fun showSkip(type: Int, toPos: Long) = lifecycleScope.launch(Dispatchers.Main) {
        with(binding) {
            if (exoPlayer.currentPosition + 2000 < toPos) {
                showingSkipType = type
                skip.text = when (type) {
                    0 -> safeContext.getString(R.string.skip_throwback)
                    1 -> safeContext.getString(R.string.skip_intro)
                    2 -> safeContext.getString(R.string.skip_outro)
                    else -> safeContext.getString(R.string.skip)
                }
                skip.setOnClickListener {
                    exoPlayer.seekTo(toPos)
                }
                skip.isEnabled = true
                skip.isClickable = true
                skip.visible()
                skip.requestFocus()
            }
        }
    }

    private fun hideSkip(type: Int) = lifecycleScope.launch(Dispatchers.Main) {
        if (showingSkipType == type) {
            with(binding) {
                skip.gone()
                skip.setOnClickListener(null)
                skip.isEnabled = false
                skip.isClickable = false
                player.requestFocus()
            }
        }
    }

    private fun checkPosition() {
        val throwBackStart = navArgs.streamConfig.throwback.start
        val throwBackEnd = navArgs.streamConfig.throwback.end
        val introStart = navArgs.streamConfig.intro.start
        val introEnd = navArgs.streamConfig.intro.end
        val outroStart = navArgs.streamConfig.outro.start
        val outroEnd = navArgs.streamConfig.outro.end

        if (throwBackStart != null && throwBackEnd != null) {
            if (exoPlayer.currentPosition in throwBackStart..throwBackEnd) {
                showSkip(0, throwBackEnd)
            }
        }
        if (introStart != null && introEnd != null) {
            if (exoPlayer.currentPosition in introStart..introEnd) {
                showSkip(1, introEnd)
            }
        }
        if (outroStart != null && outroEnd != null) {
            if (exoPlayer.currentPosition in outroStart..outroEnd) {
                showSkip(2, outroEnd)
            }
        }
    }

    private fun listenSettingsState() = settingsViewModel.data.launchAndCollect {
        binding.player.setPreviewEnabled(it.video.previewEnabled)
        if (it.video.defaultFullscreen && !binding.player.fullscreenRestored) {
            binding.player.setFullscreenState(true)
        }
    }

    private fun listenVideoSourceState() = videoViewModel.videoSourcePos.launchAndCollect {
        val nextStream = navArgs.videoStream.url.getOrElse(it) { navArgs.videoStream.url[0] }
        exoPlayer.setMediaItem(MediaItem.fromUri(nextStream))
        exoPlayer.prepare()

        withContext(Dispatchers.IO) {
            try {
                retriever.setDataSource(nextStream)
            } catch (ignored: Exception) {
                withContext(Dispatchers.Main) {
                    binding.player.setPreviewEnabled(false)
                }
            }
        }

        if (episodeInfo.currentWatchPos >= 3000L) {
            exoPlayer.seekTo(episodeInfo.currentWatchPos)
        }
        checkPosition()
        episodeInfo.totalWatchPos = exoPlayer.duration
        withContext(Dispatchers.IO) {
            burningSeriesViewModel.updateEpisodeInfo(episodeInfo)
        }
        saveWatchedPosition()
        loadNextEpisode()
    }

    override fun loadPreview(currentPosition: Long, max: Long) {
        if (currentPosition > framePosStep + 2000 || currentPosition < framePosStep - 2000) {
            framePosStep = currentPosition
            lifecycleScope.launch(Dispatchers.IO) {
                executor.execute(Schema.Conflated) {
                    val surfaceBitmap: Bitmap?
                    withContext(Dispatchers.Main) {
                        surfaceBitmap = getSurfaceBitmap()
                    }

                    val frameBitmap = getVideoFrame(currentPosition)
                    withContext(Dispatchers.Main) {
                        binding.player.setPreviewImage(
                            ImageLoader.create(frameBitmap ?: surfaceBitmap),
                            surfaceBitmap
                        )
                    }
                }
            }
        }
    }

    private fun getSurfaceBitmap(): Bitmap? {
        val safeTextureView = binding.player.videoSurfaceView as? TextureView?
        val surfaceBitmap = safeTextureView?.bitmap
        return if (surfaceBitmap != null && !surfaceBitmap.isRecycled) {
            surfaceBitmap
        } else { null }
    }

    private fun getVideoFrame(framePos: Long): Bitmap? {
        return try {
            retriever.getFrameAtTime(framePos * 1000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime
                ?: run {
                    val embedded = retriever.embeddedPicture
                    return if (embedded != null && embedded.isNotEmpty()) {
                        BitmapFactory.decodeByteArray(embedded, 0, embedded.size)
                    } else {
                        null
                    }
                }
        } catch (ignored: Exception) { null }
    }

    private fun listenPositionState() = positionState.launchAndCollect {
        exoPlayer.seekTo(it)
        checkPosition()
    }

    private fun listenPlayingState() = playingState.launchAndCollect {
        exoPlayer.playWhenReady = it
        if (it) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
        checkPosition()
    }

    private fun saveWatchedPosition() = lifecycleScope.launch(Dispatchers.IO) {
        while(true) {
            saveExecutor.execute(Schema.Conflated) {
                withContext(Dispatchers.Main) {
                    var currentPos = exoPlayer.contentPosition
                    if (currentPos > 3000) {
                        currentPos -= 3000
                    } else {
                        currentPos = 0
                    }
                    episodeInfo.currentWatchPos = currentPos
                    if (episodeInfo.totalWatchPos == 0L) {
                        episodeInfo.totalWatchPos = exoPlayer.duration
                    }
                }
                burningSeriesViewModel.updateEpisodeInfo(episodeInfo)
            }
            delay(1000)
        }
    }

    private fun loadNextEpisode() = lifecycleScope.launch(Dispatchers.IO) {
        while(nextEpisodeInfo == null) {
            executor.execute(Schema.Queue) {
                val watchedPercentage = withContext(Dispatchers.Main) {
                    (exoPlayer.contentPosition.toFloat() / exoPlayer.contentDuration.toFloat()) * 100F
                }
                if (watchedPercentage >= 85F) {
                    val nextNum = try {
                        episodeInfo.number.toInt() + 1
                    } catch (ignored: Exception) { null }

                    if (nextNum != null) {
                        nextEpisodeInfo = burningSeriesDao.getEpisodeInfoBySeriesAndNumber(episodeInfo.seriesId, nextNum.toString()).first()
                        burningSeriesViewModel.getStream(
                            nextEpisodeInfo?.hoster ?: nextEpisodeInfo?.episode?.hoster ?: listOf()
                        ).mapNotNull { it.data }.launchAndCollect { streams ->
                            nextEpisodePlainStreams.addAll(streams)
                            nextEpisodeStreams.addAll(videoViewModel.getVideoSources(streams).first())
                        }
                    }
                }
            }
            delay(1000)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        nextSourceOrDialog()
    }

    override fun onPlaybackStateChanged(playbackState: Int): Unit = with(binding) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_ENDED && nextEpisodeInfo != null && nextEpisodeStreams.isNotEmpty()) {
            val sameHosterOrFirst = nextEpisodeStreams.firstOrNull { it.hoster.equals(navArgs.videoStream.hoster, true) } ?: nextEpisodeStreams.first()
            findNavController().safeNavigate(VideoFragmentDirections.actionVideoFragmentSelf(
                sameHosterOrFirst,
                navArgs.seriesWithInfo,
                nextEpisodeInfo!!.episode,
                nextEpisodeStreams.getBestConfig(sameHosterOrFirst),
                nextEpisodeInfo!!.episode.getStreamHref(sameHosterOrFirst, nextEpisodePlainStreams)
            ))
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean? {
        return if (event != null) {
            if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                null
            } else {
                binding.player.dispatchKeyEvent(event)
            }
        } else {
            null
        }
    }

    override fun onBackPressed() {
        if (!binding.player.isLocked) {
            findNavController().popBackStack()
        }
    }

    private fun nextSourceOrDialog() {
        val currentSourcePos = videoViewModel.videoSourcePos.value
        if (currentSourcePos >= navArgs.videoStream.url.size - 1) {
            materialDialogBuilder {
                setPositiveButtonIcon(R.drawable.ic_baseline_edit_24)
                setNegativeButtonIcon(R.drawable.ic_baseline_close_24)
                setNeutralButtonIcon(R.drawable.ic_baseline_arrow_outward_24)
                builder {
                    setTitle(R.string.stream_unavailable)
                    setMessage(R.string.stream_unavailable_text)
                    setPositiveButton(R.string.activate) { dialog, _ ->
                        dialog.dismiss()
                        findNavController().safeNavigate(VideoFragmentDirections.actionVideoFragmentToScrapeHosterFragment(
                            episodeInfo.href,
                            navArgs.seriesWithInfo
                        ))
                    }
                    setNegativeButton(R.string.back) { dialog, _ ->
                        dialog.cancel()
                        findNavController().popBackStack()
                    }
                    setNeutralButton(R.string.browser) { dialog, _, ->
                        dialog.dismiss()
                        navArgs.videoStream.defaultUrl.toUri().openInBrowser(safeContext)
                    }
                }
            }.show()
        } else lifecycleScope.launch(Dispatchers.IO) {
            videoViewModel.videoSourcePos.emit(currentSourcePos + 1)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(PLAYER_POSITION, episodeInfo.currentWatchPos)
        outState.putBoolean(PLAYER_PLAYING, exoPlayer.isPlaying || autoPlay())

        super.onSaveInstanceState(outState)
    }

    private fun autoPlay(): Boolean {
        val currentPos = exoPlayer.contentPosition
        val savedPos = episodeInfo.currentWatchPos
        val inRange = currentPos in 0..1000 || currentPos in (savedPos - 500) .. (savedPos + 500)
        return inRange && (exoPlayer.playWhenReady || playingState.value)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            positionState.tryEmit(it.getLong(PLAYER_POSITION))
            playingState.tryEmit(it.getBoolean(PLAYER_PLAYING))
        }
    }

    override fun onDestroyView() {
        exoPlayer.release()
        retriever.release()
        safeActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onDestroyView()
    }

    override fun onStop() {
        exoPlayer.pause()
        safeActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onStop()
    }

    override fun initActivityViews() {
        super.initActivityViews()

        hideAppBarLayout()
        enterFullScreen()
        hideNavigationFabs()
        extendedFab?.gone()
    }

    companion object {
        const val PLAYER_POSITION = "PlayerPosition"
        const val PLAYER_PLAYING = "PlayerPlaying"
    }
}