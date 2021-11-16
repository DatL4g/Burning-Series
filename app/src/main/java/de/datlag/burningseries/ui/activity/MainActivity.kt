package de.datlag.burningseries.ui.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.burningseries.R
import de.datlag.burningseries.Secrets
import de.datlag.burningseries.databinding.ActivityMainBinding
import de.datlag.burningseries.extend.AdvancedActivity
import de.datlag.burningseries.ui.connector.*
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber

@AndroidEntryPoint
@Obfuscate
class MainActivity : AdvancedActivity(R.layout.activity_main), ToolbarContent, ToolbarImage,
	ToolbarSearch, ToolbarCollapsing, FABExtended {

	private val binding: ActivityMainBinding by viewBinding()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setSupportActionBar(binding.toolbar)
	}

	override val appBarLayout: AppBarLayout
		get() = binding.appBarLayout

	override val toolbar: Toolbar
		get() = binding.toolbar

	override val collapsingToolbar: CollapsingToolbarLayout
		get() = binding.collapsingToolbar

	override val imageView: ImageView
		get() = binding.expandedImage

	override val searchView: SimpleSearchView
		get() = binding.searchView

	override val extendedFab: ExtendedFloatingActionButton
		get() = binding.extendedFab
}