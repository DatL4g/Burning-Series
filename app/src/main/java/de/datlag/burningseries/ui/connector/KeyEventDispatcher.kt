package de.datlag.burningseries.ui.connector

import android.view.KeyEvent

interface KeyEventDispatcher {
    fun dispatchKeyEvent(event: KeyEvent?): Boolean?
}