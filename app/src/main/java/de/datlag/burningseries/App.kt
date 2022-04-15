package de.datlag.burningseries

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import de.datlag.burningseries.helper.NightMode
import de.datlag.datastore.SettingsPreferences
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
@Obfuscate
class App : MultiDexApplication() {

	@Inject
	lateinit var settingsDataStore: DataStore<SettingsPreferences>

	var themeId: Int = 0

	override fun onCreate() {
		super.onCreate()
		applyDarkMode()
		applyFont()
		
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}
	}

	private fun applyDarkMode() = GlobalScope.launch(Dispatchers.IO) {
		settingsDataStore.data.map { it.appearance }.collect {
			val mode = if (it.darkMode) NightMode.Mode.DARK else NightMode.Mode.LIGHT
			themeId = it.theme
			withContext(Dispatchers.Main) {
				AppCompatDelegate.setDefaultNightMode(mode.toDelegateMode())
			}
		}
	}

	private fun applyFont() {
		ViewPump.init(ViewPump.builder()
			.addInterceptor(CalligraphyInterceptor(
				CalligraphyConfig.Builder()
					.setDefaultFontPath(this.getString(R.string.font_path))
					.setFontAttrId(R.attr.fontPath)
					.build()
				)
			).build()
		)
	}
}