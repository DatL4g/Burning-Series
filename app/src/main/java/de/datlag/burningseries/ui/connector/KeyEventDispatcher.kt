package de.datlag.burningseries.ui.connector

import android.view.KeyEvent
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface KeyEventDispatcher {
    fun dispatchKeyEvent(event: KeyEvent?): Boolean?
}