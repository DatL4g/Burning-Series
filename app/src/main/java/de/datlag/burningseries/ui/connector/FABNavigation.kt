package de.datlag.burningseries.ui.connector

import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface FABNavigation {
    val previousFab: FloatingActionButton
    val nextFab: FloatingActionButton
}