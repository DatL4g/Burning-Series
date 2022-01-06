package de.datlag.burningseries.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fede987.statusbaralert.StatusBarAlert
import com.fede987.statusbaralert.utils.statusBarAlert
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.databinding.ActivityMainBinding
import de.datlag.burningseries.extend.AdvancedActivity
import de.datlag.burningseries.ui.connector.FABExtended
import de.datlag.burningseries.ui.connector.FABNavigation
import de.datlag.burningseries.ui.connector.KeyEventDispatcher
import de.datlag.burningseries.ui.connector.StatusBarAlertProvider
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
class MainActivity : AdvancedActivity(R.layout.activity_main), FABExtended, StatusBarAlertProvider, FABNavigation {

	private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
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

	override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
		return (getCurrentNavFragment() as? KeyEventDispatcher?)?.dispatchKeyEvent(event) ?: super.dispatchKeyEvent(event)
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