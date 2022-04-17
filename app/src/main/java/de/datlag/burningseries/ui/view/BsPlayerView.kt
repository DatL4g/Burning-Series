package de.datlag.burningseries.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dolatkia.animatedThemeManager.ThemeManager
import com.github.rubensousa.previewseekbar.PreviewLoader
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.ExoplayerControlsBinding
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.coilifier.ImageLoader
import de.datlag.coilifier.commons.load
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize

@Obfuscate
class BsPlayerView :
    StyledPlayerView,
    StyledPlayerControlView.VisibilityListener,
    LifecycleObserver
{

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val isLocked: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isFullscreen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var fullscreenRestored: Boolean = false
    private var fullscreenListener: ((Boolean) -> Unit)? = null

    private val controlsBinding: ExoplayerControlsBinding by lazy {
        val videoControlView = findViewById<View>(R.id.exoplayer_controls)
        ExoplayerControlsBinding.bind(videoControlView)
    }

    private var backPressUnit: (() -> Unit)? = null

    private val lifecycleOwner: LifecycleOwner?
        get() = findViewTreeLifecycleOwner() ?: context.getLifecycleOwner()

    init {
        initViews()
        setControllerVisibilityListener(this)

        listenLockState()
        listenFullscreenState()
    }

    private fun initViews(): Unit = with(controlsBinding) {
        if (context.packageManager.isTelevision()) {
            lockButton.hide()
            exoFullscreen.invisible()
        } else {
            lockButton.show()
            exoFullscreen.show()
        }

        backButton.setOnClickListener {
            backPressUnit?.invoke()
        }
        lockButton.setOnClickListener {
            toggleLockState()
        }
        exoFullscreen.setOnClickListener {
            toggleFullscreenState()
        }
        exoPlay.setOnClickListener {
            this@BsPlayerView.player?.play()
        }
        exoPause.setOnClickListener {
            this@BsPlayerView.player?.pause()
        }
        (ThemeManager.currentTheme as? ApplicationTheme?)?.let {
            exoProgress.setPlayedColor(it.playerSeekBarPlayedColor(context))
            exoProgress.scrubberColor = it.playerSeekBarScrubberColor(context)
        }
    }

    override fun onVisibilityChange(visibility: Int) {
        setLocked(isLocked.value)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            if (isControllerFullyVisible) {
                hideController()
                setLocked(isLocked.value)
            } else {
                showController()
                if (controlsBinding.exoPlay.isVisible) {
                    controlsBinding.exoPlay.requestFocus()
                } else {
                    controlsBinding.exoPause.requestFocus()
                }
                setLocked(isLocked.value)
            }
            return false
        }
        return true
    }

    private fun getSafeScope(): CoroutineScope {
        return lifecycleOwner?.lifecycleScope ?: GlobalScope
    }

    fun setTitle(title: String?): Unit = with(controlsBinding.title) {
        if (title.isNullOrEmpty()) {
            hide()
        } else {
            text = title
            show()
        }
    }

    fun onPlayingChanged(isPlaying: Boolean): Unit = with(controlsBinding) {
        if (isPlaying) {
            val playFocused = exoPlay.isFocused
            exoPlay.hide()
            exoPause.show()
            if (playFocused) {
                exoPause.requestFocus()
            }
        } else {
            val pauseFocused = exoPause.isFocused
            exoPause.hide()
            exoPlay.show()
            if (pauseFocused) {
                exoPlay.requestFocus()
            }
        }
    }

    fun setOnBackPressed(callback: () -> Unit) {
        backPressUnit = callback
    }

    fun setLockState(locked: Boolean) {
        isLocked.forceEmit(locked, getSafeScope())
    }

    fun toggleLockState() {
        setLockState(!isLocked.value)
    }

    private fun listenLockState() = lifecycleOwner?.let {
        isLocked.launchAndCollectIn(it) { value ->
            setLocked(value)
        }
    }

    fun setPreviewLoader(previewLoader: PreviewLoader?): Unit = with(controlsBinding.exoProgress) {
        this.setPreviewLoader(previewLoader)
    }

    fun setPreviewImage(image: ImageLoader, placeholder: Any? = null): Unit = with(controlsBinding) {
        exoProgress.isEnabled = isLocked.value
        exoProgress.isClickable = isLocked.value
        imageView.load<Drawable>(image) {
            placeholder(placeholder ?: image)
            error(placeholder ?: image)
        }
    }

    fun setPreviewEnabled(enabled: Boolean): Unit = with((controlsBinding.exoProgress)) {
        isPreviewEnabled = enabled
        isEnabled = isLocked.value
        isClickable = isLocked.value
    }

    fun setFullscreenListener(listener: (Boolean) -> Unit) {
        fullscreenListener = listener
    }

    fun setFullscreenState(fullscreen: Boolean) {
        isFullscreen.forceEmit(fullscreen, getSafeScope())
    }

    fun toggleFullscreenState() {
        setFullscreenState(!isFullscreen.value)
    }

    private fun listenFullscreenState() = lifecycleOwner?.let {
        isFullscreen.launchAndCollectIn(it) { value ->
            setFullScreen(value)
        }
    }

    private fun setLocked(toLocked: Boolean): Unit = with(controlsBinding) {
        if (toLocked) {
            lockButton.load<Drawable>(R.drawable.ic_baseline_lock_24)
            exoFullscreen.invisible()
            backButton.hide()
            exoFfwd.hide()
            exoPause.hide()
            exoPlay.hide()
            exoRew.hide()
            exoFullscreen.post { exoFullscreen.invisible() }
            backButton.post { backButton.hide() }
            exoFfwd.post { exoFfwd.hide() }
            exoPause.post { exoPause.hide() }
            exoPlay.post { exoPlay.hide() }
            exoRew.post { exoRew.hide() }
        } else {
            lockButton.load<Drawable>(R.drawable.ic_baseline_lock_open_24)
            if (!context.packageManager.isTelevision()) {
                exoFullscreen.show()
                exoFullscreen.post { exoFullscreen.show() }
            } else {
                exoFullscreen.invisible()
                exoFullscreen.post { exoFullscreen.invisible() }
            }
            backButton.show()
            backButton.post { backButton.show() }
            exoFfwd.show()
            exoFfwd.post { exoFfwd.show() }
            if (this@BsPlayerView.player?.isPlaying == true) {
                exoPause.show()
                exoPause.post { exoPause.show() }
                exoPlay.hide()
                exoPlay.post { exoPlay.hide() }
            } else {
                exoPlay.show()
                exoPlay.post { exoPlay.show() }
                exoPause.hide()
                exoPause.post { exoPause.hide() }
            }
            exoRew.show()
            exoRew.post { exoRew.show() }
        }
        exoFullscreen.isEnabled = !toLocked
        backButton.isEnabled = !toLocked
        exoFfwd.isEnabled = !toLocked
        exoFfwd.isClickable = !toLocked
        exoPause.isEnabled = !toLocked
        exoPause.isClickable = !toLocked
        exoPlay.isEnabled = !toLocked
        exoPlay.isClickable = !toLocked
        exoRew.isEnabled = !toLocked
        exoRew.isClickable = !toLocked
        exoProgress.isEnabled = !toLocked
        exoProgress.isClickable = !toLocked

        exoFullscreen.post { exoFullscreen.isEnabled = !toLocked }
        backButton.post { backButton.isEnabled = !toLocked }
        exoFfwd.post {
            exoFfwd.isEnabled = !toLocked
            exoFfwd.isClickable = !toLocked
        }
        exoPause.post {
            exoPause.isEnabled = !toLocked
            exoPause.isClickable = !toLocked
        }
        exoPlay.post {
            exoPlay.isEnabled = !toLocked
            exoPlay.isClickable = !toLocked
        }
        exoRew.post {
            exoRew.isEnabled = !toLocked
            exoRew.isClickable = !toLocked
        }
        exoProgress.post {
            exoProgress.isEnabled = !toLocked
            exoProgress.isClickable = !toLocked
        }
    }

    private fun setFullScreen(toFullScreen: Boolean) = with(controlsBinding) {
        if (toFullScreen) {
            exoFullscreen.load<Drawable>(R.drawable.ic_baseline_fullscreen_exit_24)
        } else {
            exoFullscreen.load<Drawable>(R.drawable.ic_baseline_fullscreen_24)
        }
        fullscreenListener?.invoke(toFullScreen)
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = try {
            super.onSaveInstanceState()
        } catch (ignored: Exception) {
            BaseSavedState.EMPTY_STATE
        }
        val save = try {
            SaveState(
                state,
                isLocked.value,
                isFullscreen.value,
                fullscreenListener
            )
        } catch (ignored: Exception) { state }

        return save ?: BaseSavedState.EMPTY_STATE
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val saveState = state as? SaveState?
        super.onRestoreInstanceState(saveState?.superSaveState ?: state)

        saveState?.let { save ->
            isLocked.forceEmit(save.isLocked, getSafeScope())
            isFullscreen.forceEmit(save.isFullscreen, getSafeScope())
            fullscreenRestored = true
            if (fullscreenListener == null) {
                fullscreenListener = save.fullScreenListener
            }
        }
    }

    @Parcelize
    data class SaveState(
        val superSaveState: Parcelable?,
        val isLocked: Boolean,
        val isFullscreen: Boolean,
        val fullScreenListener: ((Boolean) -> Unit)?
    ) : View.BaseSavedState(superSaveState), Parcelable
}