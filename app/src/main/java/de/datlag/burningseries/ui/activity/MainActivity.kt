package de.datlag.burningseries.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.aboutlibraries.LibsConfiguration
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.databinding.ActivityMainBinding
import de.datlag.burningseries.extend.AdvancedActivity
import de.datlag.burningseries.ui.connector.*
import io.github.florent37.shapeofview.shapes.ArcView
import io.michaelrocks.paranoid.Obfuscate


@AndroidEntryPoint
@Obfuscate
class MainActivity : AdvancedActivity(), FABExtended, FABNavigation, ToolbarInfo {

	private val binding: ActivityMainBinding by viewBinding(CreateMethod.INFLATE)

	private val navListener = NavController.OnDestinationChangedListener { _, _, _ ->
		appbarLayout.setExpandable(true)
		supportActionBar?.setDisplayHomeAsUpEnabled(false)
		supportActionBar?.setDisplayShowHomeEnabled(false)
		toolbar.setNavigationOnClickListener(null)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		if (savedInstanceState != null) {
			this.setTheme(R.style.AppTheme)
		} else {
			installSplashScreen()
		}
		WindowCompat.setDecorFitsSystemWindows(window, false)
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		LibsConfiguration.uiListener = object : LibsConfiguration.LibsUIListener {
			override fun preOnCreateView(view: View): View { return view }

			override fun postOnCreateView(view: View): View {
				appbarLayout.setExpandable(false)
				supportActionBar?.setDisplayHomeAsUpEnabled(true)
				supportActionBar?.setDisplayShowHomeEnabled(true)
				toolbar.setNavigationOnClickListener { onBackPressed() }
				return view
			}
		}

		setSupportActionBar(binding.toolbar)
		binding.searchView.setOnSearchViewListener(object : SimpleSearchView.SearchViewListener{
			override fun onSearchViewClosed() {
				onSearchClosed()
			}

			override fun onSearchViewClosedAnimation() {
				onSearchClosed()
			}

			override fun onSearchViewShown() {
				onSearchOpened()
			}

			override fun onSearchViewShownAnimation() {
				onSearchOpened()
			}
		})
		ContextCompat.getDrawable(this, R.drawable.ic_baseline_close_24)?.apply {
			colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
				ContextCompat.getColor(this@MainActivity, R.color.coloredBackgroundTextColor),
				BlendModeCompat.SRC_IN
			)
		}?.let {
			binding.searchView.setClearIconDrawable(it)
		}
	}

	private fun onSearchClosed() {
		binding.appBarLayout.setExpandable(true)
		binding.collapsingToolbar.isTitleEnabled = true
	}

	private fun onSearchOpened() {
		binding.appBarLayout.setExpanded(false, false)
		binding.appBarLayout.setExpandable(false)
		binding.collapsingToolbar.isTitleEnabled = false
	}

	override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
		return ((getCurrentNavFragment() as? KeyEventDispatcher?)?.dispatchKeyEvent(event) ?: false) || super.dispatchKeyEvent(event)
	}

	override fun onBackPressed() {
		(getCurrentNavFragment() as? BackPressedDispatcher?)?.onBackPressed() ?: super.onBackPressed()
	}

	override fun onResume() {
		super.onResume()
		findNavController(this, R.id.nav_host_fragment).addOnDestinationChangedListener(navListener)
	}

	override fun onPause() {
		super.onPause()
		findNavController(this, R.id.nav_host_fragment).removeOnDestinationChangedListener(navListener)
	}

	override val extendedFab: ExtendedFloatingActionButton
		get() = binding.extendedFab

	override val previousFab: FloatingActionButton
		get() = binding.previousFab

	override val nextFab: FloatingActionButton
		get() = binding.nextFab

	override val searchView: SimpleSearchView
		get() = binding.searchView

	override val collapsingToolbarLayout: CollapsingToolbarLayout
		get() = binding.collapsingToolbar

	override val appbarLayout: AppBarLayout
		get() = binding.appBarLayout

	override val toolbar: MaterialToolbar
		get() = binding.toolbar

	override val seriesCover: ImageView
		get() = binding.seriesCover

	override val seriesArcWrapper: LinearLayoutCompat
		get() = binding.seriesArcWrapper

	override val seriesArc: ArcView?
		get() = binding.seriesArc

	override val sizeHolder: View
		get() = binding.collapsingSizeHolder

}