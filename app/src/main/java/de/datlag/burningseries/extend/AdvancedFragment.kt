package de.datlag.burningseries.extend

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
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
import de.datlag.coilifier.ImageLoader
import de.datlag.network.m3o.M3ORepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
@Obfuscate
abstract class AdvancedFragment : Fragment {
	
	constructor() : super() { }
	
	constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId) { }
	
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
			m3oRepository.getImageFromURL(url)
				.asLiveData(lifecycleScope.coroutineContext)
				.observe(viewLifecycleOwner) {
					it.data?.let { bytes ->
						saveFileInternal(name, bytes)
						onLoaded.invoke(ImageLoader.create(bytes))
					} ?: run {
						onLoaded.invoke(null)
					}
				}
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

	override fun onDestroyView() {
		statusBarAlert?.hide()
		super.onDestroyView()
	}

	fun setSupportActionBar(toolbar: Toolbar?) {
		(safeActivity as? AppCompatActivity?)?.delegate?.setSupportActionBar(toolbar)
	}

	fun showToolbarBackButton(toolbar: Toolbar) {
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setDisplayShowHomeEnabled(true)
		toolbar.setNavigationOnClickListener { safeActivity?.onBackPressed() }
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