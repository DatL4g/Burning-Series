package de.datlag.burningseries.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dolatkia.animatedThemeManager.AppTheme
import com.fede987.statusbaralert.StatusBarAlert
import com.fede987.statusbaralert.utils.statusBarAlert
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.databinding.ActivityMainBinding
import de.datlag.burningseries.extend.AdvancedActivity
import de.datlag.burningseries.ui.connector.*
import de.datlag.burningseries.ui.theme.ApplicationTheme
import io.michaelrocks.paranoid.Obfuscate


@AndroidEntryPoint
@Obfuscate
class MainActivity : AdvancedActivity(), FABExtended, StatusBarAlertProvider, FABNavigation {

	private val binding: ActivityMainBinding by viewBinding(CreateMethod.INFLATE)
	private lateinit var statusBarAlertProvider: StatusBarAlert

	override fun onCreate(savedInstanceState: Bundle?) {
		if (savedInstanceState != null) {
			this.setTheme(R.style.AppTheme)
		} else {
			installSplashScreen()
		}
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		statusBarAlertProvider = statusBarAlert {
			autoHide(false)
			showProgress(false)
			alertColor(R.color.defaultContentColor)
			textColor(R.color.defaultBackgroundColor)
			progressBarColor(R.color.defaultBackgroundColor)
		}
	}

	override fun syncTheme(appTheme: AppTheme) {
		val currentTheme = appTheme as? ApplicationTheme?
		currentTheme?.let {
			binding.coordinator.setBackgroundColor(it.defaultBackgroundColor(this))
			binding.navHostFragment.setBackgroundColor(it.defaultBackgroundColor(this))
		}
	}

	override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
		return ((getCurrentNavFragment() as? KeyEventDispatcher?)?.dispatchKeyEvent(event) ?: false) || super.dispatchKeyEvent(event)
	}

	override fun onBackPressed() {
		(getCurrentNavFragment() as? BackPressedDispatcher?)?.onBackPressed() ?: super.onBackPressed()
	}

	override val extendedFab: ExtendedFloatingActionButton
		get() = binding.extendedFab

	override val statusBarAlert: StatusBarAlert
		get() = statusBarAlertProvider

	override val previousFab: FloatingActionButton
		get() = binding.previousFab

	override val nextFab: FloatingActionButton
		get() = binding.nextFab

}