package de.datlag.burningseries.ui.fragment

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.TextureView
import android.view.View
import android.view.WindowInsetsController
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.ExoplayerControlsBinding
import de.datlag.burningseries.databinding.FragmentVideoBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.burningseries.ui.connector.KeyEventDispatcher
import de.datlag.burningseries.ui.dialog.LoadingDialog
import de.datlag.burningseries.viewmodel.SettingsViewModel
import de.datlag.burningseries.viewmodel.VideoViewModel
import de.datlag.coilifier.commons.load
import de.datlag.coilifier.commons.loadBitmap
import de.datlag.executor.Executor
import de.datlag.executor.Schema
import de.datlag.model.Constants
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
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
    private val binding: FragmentVideoBinding by viewBinding()
    private val controlsBinding: ExoplayerControlsBinding by lazy {
        val videoControlView = binding.root.findViewById<View>(R.id.exoplayer_controls)
        ExoplayerControlsBinding.bind(videoControlView)
    }
    private val videoViewModel: VideoViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var retriever: FFmpegMediaMetadataRetriever

    private val positionState: MutableStateFlow<Long> = MutableStateFlow(0)
    private val playingState: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val isFullscreen: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @Inject
    lateinit var executor: Executor

    private var framePosStep: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayer()
        listenPreviewState()
        listenVideoSourceState()
        listenPositionState()
        listenPlayingState()
        listenFullscreen()
    }

    private fun initPlayer(): Unit = with(binding) {
        retriever = FFmpegMediaMetadataRetriever()
        try {
            retriever.setDataSource(navArgs.videoStream.url.first())
        } catch (ignored: Exception) {
            controlsBinding.exoProgress.isPreviewEnabled = false
        }

        exoPlayer = ExoPlayer.Builder(safeContext).apply {
            setSeekBackIncrementMs(10000)
            setSeekForwardIncrementMs(10000)
            setPauseAtEndOfMediaItems(true)
        }.build().apply {
            addListener(this@VideoFragment)
            playWhenReady = true
        }
        player.player = exoPlayer
        if (navArgs.title != null) {
            controlsBinding.title.text = navArgs.title
            controlsBinding.title.show()
        } else {
            controlsBinding.title.hide()
        }
        controlsBinding.exoProgress.setPreviewLoader(this@VideoFragment)
        controlsBinding.imageView.loadBitmap(getVideoFrame(-1L))
        if (isTelevision) {
            controlsBinding.exoFullscreen.invisible()
            controlsBinding.exoFullscreen.isEnabled = false
        } else {
            controlsBinding.exoFullscreen.show()
            controlsBinding.exoFullscreen.isEnabled = true
        }
        controlsBinding.exoFullscreen.setOnClickListener {
            isFullscreen.tryEmit(!isFullscreen.value)
        }
    }

    private fun listenPreviewState() = settingsViewModel.data.launchAndCollect {
        controlsBinding.exoProgress.isPreviewEnabled = it.video.previewEnabled
    }

    private fun listenVideoSourceState() = videoViewModel.videoSourcePos.launchAndCollect {
        exoPlayer.setMediaItem(MediaItem.fromUri(navArgs.videoStream.url[it]))
        exoPlayer.prepare()
    }

    override fun loadPreview(currentPosition: Long, max: Long): Unit = with(controlsBinding) {
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
                        imageView.loadBitmap(frameBitmap ?: surfaceBitmap) {
                            placeholder(surfaceBitmap)
                            error(surfaceBitmap)
                        }
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

    private fun listenFullscreen() = isFullscreen.launchAndCollect {
        setFullScreen(it)
    }

    private fun setFullScreen(toFullScreen: Boolean) = with(controlsBinding) {
        val controllerCompat = safeActivity?.window?.let { return@let WindowInsetsControllerCompat(it, it.decorView) }
        if (toFullScreen) {
            exoFullscreen.load<Drawable>(R.drawable.ic_baseline_fullscreen_exit_24)
            controllerCompat?.hide(WindowInsetsCompat.Type.systemBars())
            controllerCompat?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            safeActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            exoFullscreen.load<Drawable>(R.drawable.ic_baseline_fullscreen_24)
            controllerCompat?.show(WindowInsetsCompat.Type.systemBars())
            controllerCompat?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
            safeActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
                navArgs.bsUrl
            ))
        } else lifecycleScope.launch(Dispatchers.IO) {
            videoViewModel.videoSourcePos.emit(currentSourcePos + 1)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(PLAYER_POSITION, exoPlayer.contentPosition)
        outState.putBoolean(PLAYER_PLAYING, exoPlayer.isPlaying)
        outState.putBoolean(PLAYER_FULLSCREEN, isFullscreen.value)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            positionState.tryEmit(it.getLong(PLAYER_POSITION))
            playingState.tryEmit(it.getBoolean(PLAYER_PLAYING))
            isFullscreen.tryEmit(it.getBoolean(PLAYER_FULLSCREEN))
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
        const val PLAYER_FULLSCREEN = "PlayerFullscreen"
    }
}