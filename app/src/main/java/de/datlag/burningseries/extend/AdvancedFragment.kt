package de.datlag.burningseries.extend

import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.ui.connector.FABExtended
import de.datlag.burningseries.ui.connector.FABNavigation
import de.datlag.burningseries.ui.connector.ToolbarInfo
import de.datlag.coilifier.BlurHash
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
@Obfuscate
abstract class AdvancedFragment : Fragment {

	@ApplicationContext
	lateinit var appContext: Context

	@Inject
	@Named("coversDir")
	lateinit var coversDir: File

	@Inject
	lateinit var blurHash: BlurHash

	private var currentScrollRange = appBarLayout?.totalScrollRange ?: 0

	private val notExpandableOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
		if (!appBarLayout.isLayoutRequested && currentScrollRange != offset) {
			appBarLayout.setExpanded(false, false)
			currentScrollRange = offset
		}
	}

	fun AppBarLayout.setExpandable(expandable: Boolean) {
		if (expandable) {
			this.removeOnOffsetChangedListener(notExpandableOffsetChangedListener)
		} else {
			this.addOnOffsetChangedListener(notExpandableOffsetChangedListener)
		}
	}

	constructor() : super()
	constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

	fun getDisplayWidth(): Int {
		val windowManager = safeContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			val metrics = windowManager.currentWindowMetrics
			val windowInsets = metrics.windowInsets
			var insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
			val cutout = windowInsets.displayCutout
			if (cutout != null) {
				val cutoutSafeInsets = Insets.of(cutout.safeInsetLeft, cutout.safeInsetTop, cutout.safeInsetRight, cutout.safeInsetBottom)
				insets = Insets.max(insets, cutoutSafeInsets)
			}

			val insetsWidth = insets.right + insets.left
			val insetsHeight = insets.top + insets.bottom
			val legacySize =  Size(metrics.bounds.width() - insetsWidth, metrics.bounds.height() - insetsHeight)
			legacySize.width
		} else {
			safeContext.resources.displayMetrics.widthPixels
		}
	}

	fun loadFileSavedText(name: String): String? = if (checkFileValid(null, name)) {
		val file = safeActivity?.let { File(it.filesDir, name) }
		if (file != null && file.exists() && file.canRead()) {
			file.readText()
		} else {
			null
		}
	} else {
		null
	}

	fun fileLastModifiedOrCreated(name: String): Long = if (checkFileValid(null, name)) {
		val file = safeActivity?.let { File(it.filesDir, name) }
		file?.lastModified() ?: 0L
	} else {
		0L
	}

	fun clearTextFile(name: String) {
		val file = safeActivity?.let { File(it.filesDir, name) }
		if (file != null && file.exists() && file.canWrite()) {
			file.writeText(String())
		}
	}

	fun extendedFabFavorite(directions: NavDirections) {
		extendedFab?.let { fab ->
			fab.visibility = View.VISIBLE
			fab.text = safeContext.getString(R.string.favorites)
			fab.setIconResource(R.drawable.ic_baseline_favorite_24)
			fab.setOnClickListener {
				findNavController().safeNavigate(directions)
			}
		}
	}

	fun hideNavigationFabs() {
		previousFab?.gone()
		nextFab?.gone()
	}

	fun showNavigationFabs() {
		previousFab?.visible()
		nextFab?.visible()
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initActivityViews()
	}

	override fun onResume() {
		super.onResume()
		hideLoadingDialog()
		hideKeyboard()
		initActivityViews()
	}

	fun showToolbarBackButton(toolbar: Toolbar? = materialToolbar) {
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setDisplayShowHomeEnabled(true)
		toolbar?.setNavigationOnClickListener { safeActivity?.onBackPressed() }
	}

	fun hideToolbarBackButton(toolbar: Toolbar? = materialToolbar) {
		supportActionBar?.setDisplayHomeAsUpEnabled(false)
		supportActionBar?.setDisplayShowHomeEnabled(false)
		toolbar?.setNavigationOnClickListener(null)
	}

	fun setToolbarTitle(@StringRes resId: Int) = setToolbarTitle(safeContext.getString(resId))

	fun setToolbarTitle(title: CharSequence?) {
		collapsingToolbar?.title = title
		materialToolbar?.title = title
	}

	fun showSeriesArc() {
		toolbarInfo?.seriesCover?.visible()
		toolbarInfo?.seriesArc?.visible()
		toolbarInfo?.seriesArcWrapper?.visible()
	}

	fun hideSeriesArc() {
		toolbarInfo?.seriesCover?.gone()
		toolbarInfo?.seriesArc?.gone()
		toolbarInfo?.seriesArcWrapper?.gone()
	}

	fun enterFullScreen() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			safeActivity?.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
		}
		safeActivity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
		safeActivity?.window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
		safeActivity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
	}

	fun exitFullScreen() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			safeActivity?.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
		}
		safeActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
		val controllerCompat = safeActivity?.window?.let { controller -> return@let WindowInsetsControllerCompat(controller, controller.decorView) }
		controllerCompat?.show(WindowInsetsCompat.Type.systemBars())
		safeActivity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
	}

	fun hideAppBarLayout() {
		appBarLayout?.updateLayoutParams<CoordinatorLayout.LayoutParams> {
			height = 0
		}
	}

	fun showAppBarLayout() {
		appBarLayout?.updateLayoutParams<CoordinatorLayout.LayoutParams> {
			height = LinearLayout.LayoutParams.WRAP_CONTENT
		}
	}

	inline fun <T> Flow<T>.launchAndCollect(crossinline action: suspend CoroutineScope.(T) -> Unit) = this.launchAndCollectIn(viewLifecycleOwner, action = action)

	open fun initActivityViews() {
		searchView?.closeSearch(false)
	}

	override fun onStop() {
		appBarLayout?.setExpandable(true)
		super.onStop()
	}

	val extendedFab: ExtendedFloatingActionButton?
		get() = (safeActivity as? FABExtended?)?.extendedFab

	val supportActionBar: ActionBar?
		get() = if (safeActivity is AppCompatActivity) (safeActivity as AppCompatActivity).supportActionBar else null

	val previousFab: FloatingActionButton?
		get() = (safeActivity as? FABNavigation?)?.previousFab

	val nextFab: FloatingActionButton?
		get() = (safeActivity as? FABNavigation?)?.nextFab

	val toolbarInfo: ToolbarInfo?
		get() = safeActivity as? ToolbarInfo?

	val searchView: SimpleSearchView?
		get() = toolbarInfo?.searchView

	val collapsingToolbar: CollapsingToolbarLayout?
		get() = toolbarInfo?.collapsingToolbarLayout

	val appBarLayout: AppBarLayout?
		get() = toolbarInfo?.appbarLayout

	val materialToolbar: MaterialToolbar?
		get() = toolbarInfo?.toolbar

	val fabWrapper: ViewGroup?
		get() = (safeActivity as? FABNavigation?)?.fabWrapper
}