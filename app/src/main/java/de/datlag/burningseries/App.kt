package de.datlag.burningseries

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import de.datlag.burningseries.helper.AppUsageTracker
import de.datlag.burningseries.helper.NightMode
import de.datlag.datastore.SettingsPreferences
import de.datlag.model.Constants
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
@Obfuscate
class App : MultiDexApplication() {

	@Inject
	lateinit var settingsDataStore: DataStore<SettingsPreferences>

	override fun onCreate() {
		super.onCreate()

		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}

		applySettings()
		applyFont()

		Thread.setDefaultUncaughtExceptionHandler { _, exception ->
			val logFile = File(filesDir, Constants.LOG_FILE)

			try {
				if (!logFile.exists()) {
					try {
						logFile.createNewFile()
						try {
							logFile.setLastModified(Clock.System.now().toEpochMilliseconds())
						} catch (ignored: Exception) { }
					} catch (ignored: Exception) {
						try {
							openFileOutput(Constants.LOG_FILE, Context.MODE_PRIVATE).use {
								it.write(0)
								it.flush()
							}
							logFile.setLastModified(Clock.System.now().toEpochMilliseconds())
						} catch (ignored: Exception) { }
					}
				}

				if (logFile.exists()) {
					val logFileWriter = logFile.printWriter()
					logFileWriter.write(String())
					exception.printStackTrace(logFileWriter)
					try {
						logFileWriter.flush()
						logFileWriter.close()
					} catch (ignored: Exception) { }
					logFile.setLastModified(Clock.System.now().toEpochMilliseconds())
				}
			} catch (ignored: Throwable) { }
		}
	}

	private fun applySettings() = GlobalScope.launch(Dispatchers.IO) {
		settingsDataStore.data.collect {
			val mode = if (it.appearance.darkMode) NightMode.Mode.DARK else NightMode.Mode.LIGHT
			withContext(Dispatchers.Main) {
				registerActivityLifecycleCallbacks(AppUsageTracker(it.usage.spentTime) { spentTime ->
					saveUsage(spentTime)
				})
				AppCompatDelegate.setDefaultNightMode(mode.toDelegateMode())
			}
		}
	}

	private fun saveUsage(spentTime: Long) = GlobalScope.launch(Dispatchers.IO) {
		settingsDataStore.updateData {
			it.toBuilder().setUsage(it.usage.toBuilder().setSpentTime(spentTime).build()).build()
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