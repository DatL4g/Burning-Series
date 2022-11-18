package de.datlag.burningseries.ui.view

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.C.TrackType
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.upstream.Allocator
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.util.PriorityTaskManager

class CustomControl(
    private val allocator: DefaultAllocator,
    minBufferMs: Int,
    maxBufferMs: Int,
    bufferForPlaybackMs: Int,
    bufferForPlaybackAfterRebufferMs: Int
) : LoadControl {

    private var minBufferUs: Long = 0
    private var maxBufferUs: Long = 0
    private var bufferForPlaybackUs: Long = 0
    private var bufferForPlaybackAfterRebufferUs: Long = 0
    private var priorityTaskManager: PriorityTaskManager? = null

    private var targetBufferBytes = 0
    private var isLoading = false

    constructor() : this(
        DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
        DEFAULT_MIN_BUFFER_MS,
        DEFAULT_MAX_BUFFER_MS,
        DEFAULT_BUFFER_FOR_PLAYBACK_MS,
        DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
    )

    init {
        this.minBufferUs = VIDEO_BUFFER_SCALE_UP_FACTOR * minBufferMs * 1000L
        this.maxBufferUs = VIDEO_BUFFER_SCALE_UP_FACTOR * maxBufferMs * 1000L
        this.bufferForPlaybackUs = bufferForPlaybackMs * 1000L
        this.bufferForPlaybackAfterRebufferUs = bufferForPlaybackAfterRebufferMs * 1000L
    }

    override fun onPrepared() {
        reset(false)
    }

    override fun onTracksSelected(
        renderers: Array<Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: Array<out ExoTrackSelection>
    ) {
        targetBufferBytes = DEFAULT_VIDEO_BUFFER_SIZE
        for (i in renderers.indices) {
            targetBufferBytes += getDefaultBufferSize(renderers[i].trackType)
            if (renderers[i].trackType == C.TRACK_TYPE_VIDEO) {
                targetBufferBytes *= VIDEO_BUFFER_SCALE_UP_FACTOR
            }
        }
        allocator.setTargetBufferSize(targetBufferBytes)
    }

    override fun onStopped() {
        reset(false)
    }

    override fun onReleased() {
        reset(true)
    }

    override fun getAllocator(): Allocator {
        return allocator
    }

    override fun getBackBufferDurationUs(): Long {
        return minBufferUs
    }

    override fun retainBackBufferFromKeyframe(): Boolean {
        return false
    }

    override fun shouldContinueLoading(
        playbackPositionUs: Long, bufferedDurationUs: Long, playbackSpeed: Float
    ): Boolean {
        val wasLoading = isLoading

        computeIsBuffering(bufferedDurationUs)

        if (isLoading != wasLoading) {
            if (isLoading) {
                priorityTaskManager?.add(C.PRIORITY_PLAYBACK)
            } else {
                priorityTaskManager?.remove(C.PRIORITY_PLAYBACK)
            }
        }

        return isLoading
    }

    override fun shouldStartPlayback(
        bufferedDurationUs: Long,
        playbackSpeed: Float,
        rebuffering: Boolean,
        targetLiveOffsetUs: Long
    ): Boolean {
        val minBufferDurationUs = if (rebuffering) {
            bufferForPlaybackAfterRebufferUs
        } else {
            this.bufferForPlaybackUs
        }
        return minBufferDurationUs <= 0 || bufferedDurationUs >= minBufferDurationUs
    }

    private fun reset(resetAllocator: Boolean) {
        targetBufferBytes = 0
        if (isLoading) {
            priorityTaskManager?.remove(C.PRIORITY_PLAYBACK)
        }
        isLoading = false
        if (resetAllocator) {
            allocator.reset()
        }
    }

    private fun getDefaultBufferSize(trackType: @TrackType Int): Int {
        return when (trackType) {
            C.TRACK_TYPE_DEFAULT -> DEFAULT_MUXED_BUFFER_SIZE
            C.TRACK_TYPE_AUDIO -> DEFAULT_AUDIO_BUFFER_SIZE
            C.TRACK_TYPE_VIDEO -> DEFAULT_VIDEO_BUFFER_SIZE
            C.TRACK_TYPE_TEXT -> DEFAULT_TEXT_BUFFER_SIZE
            C.TRACK_TYPE_METADATA -> DEFAULT_METADATA_BUFFER_SIZE
            C.TRACK_TYPE_CAMERA_MOTION -> DEFAULT_CAMERA_MOTION_BUFFER_SIZE
            C.TRACK_TYPE_IMAGE -> DEFAULT_IMAGE_BUFFER_SIZE
            C.TRACK_TYPE_NONE -> 0
            C.TRACK_TYPE_UNKNOWN -> throw IllegalArgumentException()
            else -> throw IllegalArgumentException()
        }
    }

    private fun getBufferTimeState(bufferedDurationUs: Long): Int {
        return if (bufferedDurationUs > maxBufferUs) {
            ABOVE_HIGH_WATERMARK
        } else {
            if (bufferedDurationUs < minBufferUs) {
                BELOW_LOW_WATERMARK
            } else {
                BETWEEN_WATERMARKS
            }
        }
    }

    private fun computeIsBuffering(bufferedDurationUs: Long) {
        val bufferTimeState = getBufferTimeState(bufferedDurationUs)
        val targetBufferSizeReached = allocator.totalBytesAllocated >= targetBufferBytes

        isLoading = when (bufferTimeState) {
            BELOW_LOW_WATERMARK -> true
            BETWEEN_WATERMARKS -> !targetBufferSizeReached
            else -> false
        }
    }

    companion object {
        /**
         * The default minimum duration of media that the player will attempt to ensure is buffered at all
         * times, in milliseconds.
         */
        const val DEFAULT_MIN_BUFFER_MS = 50000

        /**
         * The default maximum duration of media that the player will attempt to buffer, in milliseconds.
         */
        const val DEFAULT_MAX_BUFFER_MS = 50000

        /**
         * The default duration of media that must be buffered for playback to start or resume following a
         * user action such as a seek, in milliseconds.
         */
        const val DEFAULT_BUFFER_FOR_PLAYBACK_MS = 2500

        /**
         * The default duration of media that must be buffered for playback to resume after a rebuffer, in
         * milliseconds. A rebuffer is defined to be caused by buffer depletion rather than a user action.
         */
        const val DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5000

        /** A default size in bytes for a video buffer.  */
        const val DEFAULT_VIDEO_BUFFER_SIZE = 4096 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for an audio buffer.  */
        const val DEFAULT_AUDIO_BUFFER_SIZE = 200 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for a text buffer.  */
        const val DEFAULT_TEXT_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for a metadata buffer.  */
        const val DEFAULT_METADATA_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for a camera motion buffer.  */
        const val DEFAULT_CAMERA_MOTION_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for an image buffer.  */
        const val DEFAULT_IMAGE_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for a muxed buffer (e.g. containing video, audio and text).  */
        const val DEFAULT_MUXED_BUFFER_SIZE = DEFAULT_VIDEO_BUFFER_SIZE + DEFAULT_AUDIO_BUFFER_SIZE + DEFAULT_TEXT_BUFFER_SIZE

        const val ABOVE_HIGH_WATERMARK = 0
        const val BETWEEN_WATERMARKS = 1
        const val BELOW_LOW_WATERMARK = 2

        const val VIDEO_BUFFER_SCALE_UP_FACTOR = 8
    }
}