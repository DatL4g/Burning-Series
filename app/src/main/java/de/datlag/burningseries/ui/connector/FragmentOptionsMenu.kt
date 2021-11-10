package de.datlag.burningseries.ui.connector

import android.view.Menu
import android.view.MenuInflater
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface FragmentOptionsMenu {
	fun onCreateMenu(menu: Menu, inflater: MenuInflater): Boolean
}