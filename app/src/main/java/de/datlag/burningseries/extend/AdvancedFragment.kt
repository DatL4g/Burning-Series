package de.datlag.burningseries.extend

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
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

	override fun onDestroy() {
		unCombineCollapsingToolbarWithSearchView()
		super.onDestroy()
	}

	fun combineCollapsingToolbarWithSearchView() {
		toolbarSearchView?.setOnSearchViewListener(object : SimpleSearchView.SearchViewListener {
			override fun onSearchViewShown() {
				collapsingToolbar?.title = String()
				collapsingToolbar?.setCollapsedTitleTextColor(Color.TRANSPARENT)
				appBarLayout?.setExpanded(false, false)
			}
			override fun onSearchViewShownAnimation() {
				collapsingToolbar?.title = String()
				collapsingToolbar?.setCollapsedTitleTextColor(Color.TRANSPARENT)
				appBarLayout?.setExpanded(false, true)
			}

			override fun onSearchViewClosed() {
				collapsingToolbar?.title = getString(R.string.app_name)
				collapsingToolbar?.setCollapsedTitleTextColor(safeContext.getColorCompat(R.color.defaultContentColor))
				appBarLayout?.setExpanded(true, false)
			}

			override fun onSearchViewClosedAnimation() {
				collapsingToolbar?.title = getString(R.string.app_name)
				collapsingToolbar?.setCollapsedTitleTextColor(safeContext.getColorCompat(R.color.defaultContentColor))
				appBarLayout?.setExpanded(true, true)
			}
		})
	}

	fun unCombineCollapsingToolbarWithSearchView() {
		toolbarSearchView?.setOnSearchViewListener(null)
	}

	val appBarLayout: AppBarLayout?
		get() = if (safeActivity is ToolbarContent) (safeActivity as ToolbarContent).appBarLayout else null

	val toolbar: Toolbar?
		get() = if (safeActivity is ToolbarContent) (safeActivity as ToolbarContent).toolbar else null

	val collapsingToolbar: CollapsingToolbarLayout?
		get() = if (safeActivity is ToolbarCollapsing) (safeActivity as ToolbarCollapsing).collapsingToolbar else null

	val toolbarImage: ImageView?
		get() = if (safeActivity is ToolbarImage) (safeActivity as ToolbarImage).imageView else null
	
	val toolbarSearchView: SimpleSearchView?
		get() = if (safeActivity is ToolbarSearch) (safeActivity as ToolbarSearch).searchView else null

	val extendedFab: ExtendedFloatingActionButton?
		get() = if (safeActivity is FABExtended) (safeActivity as FABExtended).extendedFab else null
}