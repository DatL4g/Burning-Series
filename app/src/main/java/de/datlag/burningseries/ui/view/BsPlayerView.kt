package de.datlag.burningseries.ui.view

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.findFragment
import androidx.lifecycle.*
import com.github.rubensousa.previewseekbar.PreviewLoader
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.databinding.ExoplayerControlsBinding
import de.datlag.burningseries.ui.fragment.VideoFragment
import de.datlag.coilifier.ImageLoader
import de.datlag.coilifier.commons.load
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize

@Obfuscate
class BsPlayerView : PlayerView, PlayerControlView.VisibilityListener, LifecycleObserver {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val isLocked: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isFullscreen: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val controlsBinding: ExoplayerControlsBinding by lazy {
        val videoControlView = findViewById<View>(R.id.exoplayer_controls)
        ExoplayerControlsBinding.bind(videoControlView)
    }

    private var backPressUnit: (() -> Unit)? = null
    private val safeActivity: Activity?
        get() = try {
            findFragment<VideoFragment>().safeActivity ?: this.context.getActivity()
        } catch (ignored: Exception) {
            this.context.getActivity()
        }

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
            exoFullscreen.hide()
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
    }

    override fun onVisibilityChange(visibility: Int) {
        setLocked(isLocked.value)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            if (isControllerVisible) {
                hideController()
                setLocked(isLocked.value)
            } else {
                showController()
                setLocked(isLocked.value)
            }
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

    private fun setLocked(toLocked: Boolean, exit: Boolean = false): Unit = with(controlsBinding) {
        if (!exit) {
            this@BsPlayerView.post { setLocked(toLocked, true) }
        }
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

    override fun onSaveInstanceState(): Parcelable {
        return SaveState(
            super.onSaveInstanceState(),
            isLocked.value,
            isFullscreen.value
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val saveState = state as? SaveState
        super.onRestoreInstanceState(saveState?.superSaveState ?: state)

        saveState?.let { save ->
            isLocked.forceEmit(save.isLocked, getSafeScope())
            isFullscreen.forceEmit(save.isFullscreen, getSafeScope())
        }
    }

    @Parcelize
    data class SaveState(
        val superSaveState: Parcelable?,
        val isLocked: Boolean,
        val isFullscreen: Boolean
    ) : View.BaseSavedState(superSaveState), Parcelable
}