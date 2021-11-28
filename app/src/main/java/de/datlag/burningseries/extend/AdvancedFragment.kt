package de.datlag.burningseries.extend

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.fede987.statusbaralert.StatusBarAlert
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.burningseries.R
import de.datlag.burningseries.common.*
import de.datlag.burningseries.ui.connector.*
import de.datlag.network.m3o.M3ORepository
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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
		onLoaded: (ByteArray?) -> Unit
	) {
		loadFileInternal(name)?.let {
			onLoaded.invoke(it)
		} ?: run {
			m3oRepository.getImageFromURL(url)
				.asLiveData(lifecycleScope.coroutineContext)
				.observe(viewLifecycleOwner) {
				it.data?.let { bytes ->
					saveFileInternal(name, bytes)
					onLoaded.invoke(bytes)
				} ?: run {
					onLoaded.invoke(null)
				}
			}
		}
	}

	fun extendedFabFavorite(directions: NavDirections) {
		extendedFab?.let { fab ->
			fab.show()
			fab.text = safeContext.getString(R.string.favorites)
			fab.setIconResource(R.drawable.ic_baseline_favorite_24)
			fab.setOnClickListener {
				findNavController().navigate(directions)
			}
		}
	}

	override fun onDestroyView() {
		statusBarAlert?.hide()
		super.onDestroyView()
	}

	inline fun <T> Flow<T>.launchAndCollect(crossinline action: suspend CoroutineScope.(T) -> Unit) = this.launchAndCollectIn(viewLifecycleOwner, action = action)

	val extendedFab: ExtendedFloatingActionButton?
		get() = if (safeActivity is FABExtended) (safeActivity as FABExtended).extendedFab else null

	val statusBarAlert: StatusBarAlert?
		get() = if (safeActivity is StatusBarAlertProvider) (safeActivity as StatusBarAlertProvider).statusBarAlert else null
}