package de.datlag.burningseries.extend

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.burningseries.common.loadFileInternal
import de.datlag.burningseries.common.safeActivity
import de.datlag.burningseries.common.saveFileInternal
import de.datlag.burningseries.ui.connector.ToolbarImage
import de.datlag.burningseries.ui.connector.ToolbarSearch
import de.datlag.network.m3o.M3ORepository
import io.michaelrocks.paranoid.Obfuscate
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
	
	val toolbarImage: ImageView?
		get() = if (safeActivity is ToolbarImage) (safeActivity as ToolbarImage).imageView else null
	
	val toolbarSearchView: SimpleSearchView?
		get() = if (safeActivity is ToolbarSearch) (safeActivity as ToolbarSearch).searchView else null
}