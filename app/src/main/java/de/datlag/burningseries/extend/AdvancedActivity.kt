package de.datlag.burningseries.extend

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.burningseries.App
import de.datlag.burningseries.ui.theme.BurningSeriesTheme
import de.datlag.burningseries.ui.theme.DefaultTheme
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
abstract class AdvancedActivity() : ThemeActivity() {

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

	override fun getStartTheme(): AppTheme {
		return when (((appContext ?: applicationContext) as? App?)?.themeId ?: 0) {
			1 -> BurningSeriesTheme()
			else -> DefaultTheme()
		}
	}

	companion object {
		init {
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}
	}
}