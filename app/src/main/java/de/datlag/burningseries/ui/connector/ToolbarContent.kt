package de.datlag.burningseries.ui.connector

import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface ToolbarContent {
	
	val appBarLayout: AppBarLayout
	val toolbar: Toolbar
}