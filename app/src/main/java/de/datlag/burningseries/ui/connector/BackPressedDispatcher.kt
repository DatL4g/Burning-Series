package de.datlag.burningseries.ui.connector

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface BackPressedDispatcher {
    fun onBackPressed()
}