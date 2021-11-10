package de.datlag.burningseries.ui.activity

import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.databinding.ActivityMainBinding
import de.datlag.burningseries.extend.AdvancedActivity
import de.datlag.burningseries.ui.connector.ToolbarContent
import de.datlag.burningseries.ui.connector.ToolbarImage
import de.datlag.burningseries.ui.connector.ToolbarSearch
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
class MainActivity : AdvancedActivity(R.layout.activity_main), ToolbarContent, ToolbarImage, ToolbarSearch {
	
	private val binding: ActivityMainBinding by viewBinding()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		setSupportActionBar(binding.toolbar)
		binding.searchView.setOnSearchViewListener(object : SimpleSearchView.SearchViewListener {
			override fun onSearchViewClosed() {
				binding.collapsingToolbar.title = getString(R.string.app_name)
			}
			override fun onSearchViewClosedAnimation() {
				binding.collapsingToolbar.title = getString(R.string.app_name)
			}
			
			override fun onSearchViewShown() {
				binding.appBarLayout.setExpanded(false, false)
				binding.collapsingToolbar.title = String()
			}
			override fun onSearchViewShownAnimation() {
				binding.appBarLayout.setExpanded(false, true)
				binding.collapsingToolbar.title = String()
			}
		})
	}
	
	override val appBarLayout: AppBarLayout
		get() = binding.appBarLayout
	
	override val toolbar: Toolbar
		get() = binding.toolbar
	
	override val imageView: ImageView
		get() = binding.expandedImage
	
	override val searchView: SimpleSearchView
		get() = binding.searchView
}