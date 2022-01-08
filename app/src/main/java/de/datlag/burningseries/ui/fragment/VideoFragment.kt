package de.datlag.burningseries.ui.fragment

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.KeyEvent
import android.view.TextureView
import android.view.View
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
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.FragmentVideoBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.ui.connector.KeyEventDispatcher
import de.datlag.burningseries.viewmodel.BurningSeriesViewModel
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.burningseries.viewmodel.VideoViewModel
import de.datlag.coilifier.ImageLoader
import de.datlag.executor.Executor
import de.datlag.executor.Schema
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import wseemann.media.FFmpegMediaMetadataRetriever
import javax.inject.Inject

@AndroidEntryPoint
@Obfuscate
class VideoFragment : AdvancedFragment(R.layout.fragment_video), PreviewLoader, Player.Listener, KeyEventDispatcher {

    private val navArgs: VideoFragmentArgs by navArgs()
    private val binding: FragmentVideoBinding by viewBinding(FragmentVideoBinding::bind)

    private val videoViewModel: VideoViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val burningSeriesViewModel: BurningSeriesViewModel by activityViewModels()
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var retriever: FFmpegMediaMetadataRetriever

    private val positionState: MutableStateFlow<Long> = MutableStateFlow(0)
    private val playingState: MutableStateFlow<Boolean> = MutableStateFlow(true)


    @Inject
    lateinit var executor: Executor

    @Inject
    lateinit var saveExecutor: Executor

    private var framePosStep: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayer()
        listenSettingsState()
        listenVideoSourceState()
        listenPositionState()
        listenPlayingState()
    }

    private fun initPlayer(): Unit = with(binding) {
        retriever = FFmpegMediaMetadataRetriever()

        exoPlayer = ExoPlayer.Builder(safeContext).apply {
            setSeekBackIncrementMs(10000)
            setSeekForwardIncrementMs(10000)
            setPauseAtEndOfMediaItems(true)
        }.build().apply {
            addListener(this@VideoFragment)
            playWhenReady = true
        }
        player.player = exoPlayer

        player.setTitle(navArgs.episodeInfo.title)
        player.setPreviewLoader(this@VideoFragment)
        player.setPreviewImage(ImageLoader.create(getVideoFrame(-1L)))

        player.setOnBackPressed {
            findNavController().navigate(VideoFragmentDirections.actionVideoFragmentToSeriesFragment(
                seriesWithInfo = navArgs.seriesWithInfo
            ))
        }
    }

    private fun listenSettingsState() = settingsViewModel.data.launchAndCollect {
        binding.player.setPreviewEnabled(it.video.previewEnabled)
        if (it.video.defaultFullscreen) {
            binding.player.setFullscreenState(true)
        }
    }

    private fun listenVideoSourceState() = videoViewModel.videoSourcePos.launchAndCollect {
        exoPlayer.setMediaItem(MediaItem.fromUri(navArgs.videoStream.url[it]))
        exoPlayer.prepare()

        withContext(Dispatchers.IO) {
            try {
                retriever.setDataSource(navArgs.videoStream.url[it])
            } catch (ignored: Exception) {
                withContext(Dispatchers.Main) {
                    binding.player.setPreviewEnabled(false)
                }
            }
        }

        exoPlayer.seekTo(navArgs.episodeInfo.currentWatchPos)
        navArgs.episodeInfo.totalWatchPos = exoPlayer.duration
        withContext(Dispatchers.IO) {
            burningSeriesViewModel.updateEpisodeInfo(navArgs.episodeInfo)
        }
        saveWatchedPosition()
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

    private fun saveWatchedPosition() = lifecycleScope.launch(Dispatchers.Default) {
        while(true) {
            saveExecutor.execute(Schema.Conflated) {
                withContext(Dispatchers.Main) {
                    navArgs.episodeInfo.currentWatchPos = exoPlayer.contentPosition
                    if (navArgs.episodeInfo.totalWatchPos == 0L) {
                        navArgs.episodeInfo.totalWatchPos = exoPlayer.duration
                    }
                }
                burningSeriesViewModel.updateEpisodeInfo(navArgs.episodeInfo)
            }
            delay(3000)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> nextSourceOrDialog()
            PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> nextSourceOrDialog()
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> nextSourceOrDialog()
            else -> nextSourceOrDialog()
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
            findNavController().navigate(VideoFragmentDirections.actionVideoFragmentToStreamUnavailableDialog(
                navArgs.seriesWithInfo,
                navArgs.videoStream.defaultUrl,
                navArgs.episodeInfo.href
            ))
        } else lifecycleScope.launch(Dispatchers.IO) {
            videoViewModel.videoSourcePos.emit(currentSourcePos + 1)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var currentPos = exoPlayer.contentPosition
        if (currentPos >= 3000) {
            currentPos -= 3000
        } else if (currentPos < 3000) {
            currentPos = 0
        }
        outState.putLong(PLAYER_POSITION, currentPos)
        outState.putBoolean(PLAYER_PLAYING, exoPlayer.isPlaying || exoPlayer.playWhenReady)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            positionState.tryEmit(it.getLong(PLAYER_POSITION))
            playingState.tryEmit(it.getBoolean(PLAYER_PLAYING))
        }
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
        hideLoadingDialog()
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