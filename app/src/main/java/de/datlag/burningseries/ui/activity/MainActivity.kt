package de.datlag.burningseries.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.databinding.ActivityMainBinding
import de.datlag.burningseries.extend.AdvancedActivity
import de.datlag.burningseries.ui.connector.*
import io.github.florent37.shapeofview.shapes.ArcView
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber


@AndroidEntryPoint
@Obfuscate
class MainActivity : AdvancedActivity(), FABExtended, FABNavigation, ToolbarSearchView,
	ToolbarCollapsingLayout, ToolbarAppbarLayout, ToolbarMaterialToolbar, ToolbarInfo {

	private val binding: ActivityMainBinding by viewBinding(CreateMethod.INFLATE)

	override fun onCreate(savedInstanceState: Bundle?) {
		if (savedInstanceState != null) {
			this.setTheme(R.style.AppTheme)
		} else {
			installSplashScreen()
		}
		WindowCompat.setDecorFitsSystemWindows(window, false)
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)
		binding.searchView.setOnSearchViewListener(object : SimpleSearchView.SearchViewListener{
			override fun onSearchViewClosed() {
				binding.appBarLayout.setExpandable(true)
				binding.collapsingToolbar.isTitleEnabled = true
			}

			override fun onSearchViewClosedAnimation() {
				binding.appBarLayout.setExpandable(true)
				binding.collapsingToolbar.isTitleEnabled = true
			}

			override fun onSearchViewShown() {
				binding.appBarLayout.setExpanded(false, false)
				binding.appBarLayout.setExpandable(false)
				binding.collapsingToolbar.isTitleEnabled = false
			}

			override fun onSearchViewShownAnimation() {
				binding.appBarLayout.setExpanded(false, false)
				binding.appBarLayout.setExpandable(false)
				binding.collapsingToolbar.isTitleEnabled = false
			}
		})
	}

	override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
		return ((getCurrentNavFragment() as? KeyEventDispatcher?)?.dispatchKeyEvent(event) ?: false) || super.dispatchKeyEvent(event)
	}

	override fun onBackPressed() {
		(getCurrentNavFragment() as? BackPressedDispatcher?)?.onBackPressed() ?: super.onBackPressed()
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

}