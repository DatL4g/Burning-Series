package de.datlag.burningseries

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber

@HiltAndroidApp
@Obfuscate
class App : MultiDexApplication() {
	override fun onCreate() {
		super.onCreate()
		applyFont()
		
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
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