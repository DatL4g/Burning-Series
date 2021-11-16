package de.datlag.burningseries.ui.connector

import com.ferfalk.simplesearchview.SimpleSearchView
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface ToolbarSearch {
	
	val searchView: SimpleSearchView
}