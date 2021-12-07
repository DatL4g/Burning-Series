package de.datlag.burningseries.ui.fragment

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.TextureView
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.rubensousa.previewseekbar.PreviewLoader
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.common.safeActivity
import de.datlag.burningseries.common.safeContext
import de.datlag.burningseries.databinding.ExoplayerControlsBinding
import de.datlag.burningseries.databinding.FragmentVideoBinding
import de.datlag.burningseries.extend.AdvancedFragment
import de.datlag.coilifier.commons.loadBitmap
import de.datlag.executor.Executor
import de.datlag.executor.Schema
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wseemann.media.FFmpegMediaMetadataRetriever
import javax.inject.Inject

@AndroidEntryPoint
@Obfuscate
class VideoFragment : AdvancedFragment(R.layout.fragment_video), PreviewLoader {

    private val navArgs: VideoFragmentArgs by navArgs()
    private val binding: FragmentVideoBinding by viewBinding()
    private val controlsBinding: ExoplayerControlsBinding by lazy {
        val videoControlView = binding.root.findViewById<View>(R.id.exoplayer_controls)
        ExoplayerControlsBinding.bind(videoControlView)
    }
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var retriever: FFmpegMediaMetadataRetriever

    @Inject
    lateinit var executor: Executor

    private var framePosStep: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayer()
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
        }.build().apply {
            setMediaItem(MediaItem.fromUri(navArgs.videoStream.url.first()))
            prepare()
            playWhenReady = true
        }
        player.player = exoPlayer
        controlsBinding.exoProgress.setPreviewLoader(this@VideoFragment)
        controlsBinding.imageView.loadBitmap(getVideoFrame(-1L))
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(PLAYER_POSITION, exoPlayer.contentPosition)
        outState.putBoolean(PLAYER_PLAYING, exoPlayer.isPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            val pos = it.getLong(PLAYER_POSITION)
            exoPlayer.seekTo(pos)
            exoPlayer.playWhenReady = it.getBoolean(PLAYER_PLAYING)
        }
    }

    override fun onResume() {
        super.onResume()
        extendedFab?.visibility = View.GONE
        hideNavigationFabs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer.release()
        retriever.release()
    }

    companion object {
        const val PLAYER_POSITION = "PlayerPosition"
        const val PLAYER_PLAYING = "PlayerPlaying"
    }
}