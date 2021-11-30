package de.datlag.burningseries.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentContainerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fede987.statusbaralert.StatusBarAlert
import com.fede987.statusbaralert.utils.statusBarAlert
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.Secrets
import de.datlag.burningseries.databinding.ActivityMainBinding
import de.datlag.burningseries.extend.AdvancedActivity
import de.datlag.burningseries.ui.connector.*
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
class MainActivity : AdvancedActivity(R.layout.activity_main), FABExtended, StatusBarAlertProvider, FABNavigation {

	private val binding: ActivityMainBinding by viewBinding()
	private lateinit var statusBarAlertProvided: StatusBarAlert

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		statusBarAlertProvided = statusBarAlert {
			autoHide(false)
			showProgress(false)
			alertColor(R.color.defaultContentColor)
			textColor(R.color.defaultBackgroundColor)
			progressBarColor(R.color.defaultBackgroundColor)
		}
	}

	override val extendedFab: ExtendedFloatingActionButton
		get() = binding.extendedFab

	override val statusBarAlert: StatusBarAlert
		get() = statusBarAlertProvided

	override val previousFab: FloatingActionButton
		get() = binding.previousFab

	override val nextFab: FloatingActionButton
		get() = binding.nextFab

}