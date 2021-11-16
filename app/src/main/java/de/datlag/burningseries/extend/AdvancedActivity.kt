package de.datlag.burningseries.extend

import android.content.Context
import android.view.Menu
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.burningseries.ui.connector.FragmentOptionsMenu
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
abstract class AdvancedActivity : AppCompatActivity {
	
	constructor() : super() { }
	
	constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId) { }
	
	@ApplicationContext
	var appContext: Context? = null
	
	
	override fun attachBaseContext(newBase: Context?) {
		when {
			newBase != null -> super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
			appContext != null -> super.attachBaseContext(ViewPumpContextWrapper.wrap(appContext!!))
			else -> super.attachBaseContext(newBase)
		}
	}
	
	open fun getCurrentNavFragment(): Fragment? {
		val navHostFragment = supportFragmentManager.primaryNavigationFragment
		val fragmentList = navHostFragment?.childFragmentManager?.fragments
		return if (!fragmentList.isNullOrEmpty()) fragmentList[0] else null
	}
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val currentFragment = getCurrentNavFragment()
		return if (currentFragment is FragmentOptionsMenu) {
			(currentFragment as FragmentOptionsMenu).onCreateMenu(menu, menuInflater)
		} else {
			super.onCreateOptionsMenu(menu)
		}
	}
	
	companion object {
		init {
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}
	}
}