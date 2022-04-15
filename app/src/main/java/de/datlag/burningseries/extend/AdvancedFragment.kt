package de.datlag.burningseries.extend

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.dolatkia.animatedThemeManager.ThemeFragment
import com.dolatkia.animatedThemeManager.ThemeManager
import com.fede987.statusbaralert.StatusBarAlert
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.ui.connector.FABExtended
import de.datlag.burningseries.ui.connector.FABNavigation
import de.datlag.burningseries.ui.connector.StatusBarAlertProvider
import de.datlag.burningseries.ui.theme.ApplicationTheme
import de.datlag.coilifier.ImageLoader
import de.datlag.network.m3o.M3ORepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
@Obfuscate
abstract class AdvancedFragment() : ThemeFragment() {

	@ApplicationContext
	lateinit var appContext: Context

	@Inject
	lateinit var m3oRepository: M3ORepository

	fun loadImageAndSave(
		url: String,
		name: String = url.substringAfterLast("/"),
		onLoaded: (ImageLoader?) -> Unit
	) {
		if (checkFileValid(name)) {
			safeActivity?.let {
				onLoaded.invoke(ImageLoader.create(File(it.filesDir, name)))
			}
		} else {
			m3oRepository.getImageFromURL(url).launchAndCollect {
				it.data?.let { bytes ->
					saveFileInternal(name, bytes)
					onLoaded.invoke(ImageLoader.create(bytes))
				} ?: run {
					onLoaded.invoke(null)
				}
			}
		}
	}

	fun loadSavedImage(name: String): ImageLoader? {
		return if (checkFileValid(name)) {
			safeActivity?.let {
				ImageLoader.create(File(it.filesDir, name))
			}
		} else {
			null
		}
	}

	fun getBurningSeriesHosterCount(onLoaded: (Long) -> Unit) {
		m3oRepository.getBurningSeriesHosterCount().launchAndCollect {
			if (it != null) {
				onLoaded.invoke(it)
			}
		}
	}

	fun extendedFabFavorite(directions: NavDirections) {
		extendedFab?.let { fab ->
			fab.visibility = View.VISIBLE
			fab.text = safeContext.getString(R.string.favorites)
			fab.setIconResource(R.drawable.ic_baseline_favorite_24)
			fab.setOnClickListener {
				findNavController().navigate(directions)
			}
		}
	}

	fun hideNavigationFabs() {
		previousFab?.visibility = View.GONE
		nextFab?.visibility = View.GONE
	}

	fun showNavigationFabs() {
		previousFab?.visibility = View.VISIBLE
		nextFab?.visibility = View.VISIBLE
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		hideLoadingDialog()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		statusBarAlert?.hide(false)
		hideLoadingDialog()
	}

	override fun onResume() {
		super.onResume()
		statusBarAlert?.hide(false)
		hideLoadingDialog()
		hideKeyboard()
		(ThemeManager.instance.getCurrentTheme() as? ApplicationTheme?)?.let {
			applyFabColors(it)
		}
	}

	override fun onPause() {
		super.onPause()
		statusBarAlert?.hide(false)
		hideLoadingDialog()
	}

	fun setSupportActionBar(toolbar: Toolbar?) {
		(safeActivity as? AppCompatActivity?)?.delegate?.setSupportActionBar(toolbar)
	}

	fun showToolbarBackButton(toolbar: Toolbar) {
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setDisplayShowHomeEnabled(true)
		toolbar.setNavigationOnClickListener { safeActivity?.onBackPressed() }
	}

	fun showLoadingStatusBar() = statusBarAlert?.showLoading(
		safeContext.getString(R.string.loading),
		R.color.defaultContentColor,
		R.color.defaultBackgroundColor
	)

	fun showErrorStatusBar() = statusBarAlert?.showError(
		safeContext.getString(R.string.error),
		R.color.errorBackgroundColor,
		R.color.errorContentColor
	)

	fun showSuccessStatusBar() = statusBarAlert?.showSuccess(
		safeContext.getString(R.string.success),
		R.color.successBackgroundColor,
		R.color.successContentColor
	)

	protected fun applyFabColors(appTheme: ApplicationTheme) {
		extendedFab?.setBackgroundColor(appTheme.defaultContentColor(safeContext))
		extendedFab?.setTextColor(appTheme.defaultBackgroundColor(safeContext))
		extendedFab?.iconTint = ColorStateList.valueOf(appTheme.defaultBackgroundColor(safeContext))
		nextFab?.setBackgroundColor(appTheme.defaultContentColor(safeContext))
		nextFab?.supportImageTintList = ColorStateList.valueOf(appTheme.defaultBackgroundColor(safeContext))
		previousFab?.setBackgroundColor(appTheme.defaultContentColor(safeContext))
		previousFab?.supportImageTintList = ColorStateList.valueOf(appTheme.defaultBackgroundColor(safeContext))
	}

	inline fun <T> Flow<T>.launchAndCollect(crossinline action: suspend CoroutineScope.(T) -> Unit) = this.launchAndCollectIn(viewLifecycleOwner, action = action)

	val extendedFab: ExtendedFloatingActionButton?
		get() = if (safeActivity is FABExtended) (safeActivity as FABExtended).extendedFab else null

	val statusBarAlert: StatusBarAlert?
		get() = if (safeActivity is StatusBarAlertProvider) (safeActivity as StatusBarAlertProvider).statusBarAlert else null

	val supportActionBar: ActionBar?
		get() = if (safeActivity is AppCompatActivity) (safeActivity as AppCompatActivity).supportActionBar else null

	val previousFab: FloatingActionButton?
		get() = if (safeActivity is FABNavigation) (safeActivity as FABNavigation).previousFab else null

	val nextFab: FloatingActionButton?
		get() = if (safeActivity is FABNavigation) (safeActivity as FABNavigation).nextFab else null

}