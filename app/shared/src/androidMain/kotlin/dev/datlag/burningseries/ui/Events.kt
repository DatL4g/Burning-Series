package dev.datlag.burningseries.ui

import android.app.RemoteAction
import android.view.KeyEvent

var KeyEventDispatcher: (event: KeyEvent?) -> Boolean? = { null }
var PIPEventDispatcher: () -> Boolean? = { null }
var PIPModeListener: (Boolean) -> Unit = { }
var PIPActions: () -> ArrayList<RemoteAction>? = { null }