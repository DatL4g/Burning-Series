package de.datlag.burningseries.extend

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.burningseries.ui.connector.ToolbarInfo
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
abstract class AdvancedActivity() : AppCompatActivity() {

	@ApplicationContext
	var appContext: Context? = null

	private var currentScrollRange = try {
		appBarLayout?.totalScrollRange
	} catch (ignored: Throwable) { null } ?: 0

	private val notExpandableOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
		if (!appBarLayout.isLayoutRequested && currentScrollRange != offset) {
			appBarLayout.setExpanded(false, false)
			currentScrollRange = offset
		}
	}

	fun AppBarLayout.setExpandable(expandable: Boolean) {
		if (expandable) {
			this.setLiftableOverrideEnabled(true)
			this.setLiftable(true)
			this.removeOnOffsetChangedListener(notExpandableOffsetChangedListener)
		} else {
			this.setLiftableOverrideEnabled(true)
			this.setLiftable(false)
			this.isLifted = true
			this.addOnOffsetChangedListener(notExpandableOffsetChangedListener)
		}
	}

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

	private val appBarLayout: AppBarLayout?
		get() = (this as? ToolbarInfo?)?.appbarLayout

	companion object {
		init {
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}
	}
}