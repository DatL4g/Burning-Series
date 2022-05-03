package de.datlag.burningseries.ui.fragment

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dolatkia.animatedThemeManager.AppTheme
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
import de.datlag.burningseries.common.hideLoadingDialog
import de.datlag.burningseries.common.safeActivity
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.common.safeNavigate
import de.datlag.burningseries.databinding.FragmentVideoBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.helper.lazyMutable
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
class VideoFragment : AdvancedFragment(R.layout.fragment_video), PreviewLoader, Player.Listener, KeyEventDispatcher {

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
    private var nextEpisodeStreams: MutableSet<VideoStream> = mutableSetOf()


    @Inject
    lateinit var executor: Executor

    @Inject
    lateinit var saveExecutor: Executor

    @Inject
    lateinit var burningSeriesDao: BurningSeriesDao

    private var framePosStep: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findActualEpisodeInfo()
        initPlayer()
        listenSettingsState()
        listenVideoSourceState()
        listenPositionState()
        listenPlayingState()
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
        exoPlayer = ExoPlayer.Builder(safeContext).apply {
            setSeekBackIncrementMs(10000)
            setSeekForwardIncrementMs(10000)
            setPauseAtEndOfMediaItems(true)
            setMediaSourceFactory(DefaultMediaSourceFactory(safeContext, extractorFactory))
        }.build().apply {
            addListener(this@VideoFragment)
            playWhenReady = true
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
            findNavController().safeNavigate(VideoFragmentDirections.actionVideoFragmentToSeriesFragment(
                seriesWithInfo = navArgs.seriesWithInfo
            ))
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
    }

    private fun listenPlayingState() = playingState.launchAndCollect {
        exoPlayer.playWhenReady = it
        if (it) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
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
                        burningSeriesViewModel.getStream(nextEpisodeInfo?.hoster ?: nextEpisodeInfo?.episode?.hoster ?: listOf()).mapNotNull { it.data }.launchAndCollect {
                            nextEpisodeStreams.addAll(videoViewModel.getVideoSources(it).first())
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

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        binding.player.onPlayingChanged(isPlaying)
    }

    override fun onPlaybackStateChanged(playbackState: Int): Unit = with(binding) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_ENDED && nextEpisodeInfo != null && nextEpisodeStreams.isNotEmpty()) {
            val sameHosterOrFirst = nextEpisodeStreams.firstOrNull { it.hoster.equals(navArgs.videoStream.hoster, true) } ?: nextEpisodeStreams.first()
            findNavController().safeNavigate(VideoFragmentDirections.actionVideoFragmentSelf(sameHosterOrFirst, navArgs.seriesWithInfo, nextEpisodeInfo!!.episode))
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

    private fun nextSourceOrDialog() {
        val currentSourcePos = videoViewModel.videoSourcePos.value
        if (currentSourcePos >= navArgs.videoStream.url.size - 1) {
            if (view != null) {
                findNavController().safeNavigate(VideoFragmentDirections.actionVideoFragmentToStreamUnavailableDialog(
                    navArgs.seriesWithInfo,
                    navArgs.videoStream.defaultUrl,
                    episodeInfo.href
                ))
            }
        } else lifecycleScope.launch(Dispatchers.IO) {
            videoViewModel.videoSourcePos.emit(currentSourcePos + 1)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val currentPos = exoPlayer.contentPosition
        val savedPos = episodeInfo.currentWatchPos
        val inRange = currentPos in 0..1000 || currentPos in (savedPos - 500) .. (savedPos + 500)
        outState.putLong(PLAYER_POSITION, episodeInfo.currentWatchPos)
        outState.putBoolean(PLAYER_PLAYING, exoPlayer.isPlaying || (exoPlayer.playWhenReady && inRange))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            positionState.tryEmit(it.getLong(PLAYER_POSITION))
            playingState.tryEmit(it.getBoolean(PLAYER_PLAYING))
        }
    }

    override fun syncTheme(appTheme: AppTheme) { }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
        hideLoadingDialog()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            safeActivity?.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        safeActivity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        safeActivity?.window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        safeActivity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            safeActivity?.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
        safeActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        safeActivity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, true) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer.release()
        retriever.release()
        safeActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    companion object {
        const val PLAYER_POSITION = "PlayerPosition"
        const val PLAYER_PLAYING = "PlayerPlaying"
    }
}