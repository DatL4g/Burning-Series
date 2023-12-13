package dev.datlag.burningseries.shared.ui

import android.app.RemoteAction
import android.view.KeyEvent

var SmallIcon: Int = 0
var NotificationPermission: Boolean = false

var KeyEventDispatcher: (event: KeyEvent?) -> Boolean? = { null }
var PIPEnabled: Boolean = false
var PIPModeListener: (Boolean) -> Unit = { }
var PIPActions: () -> ArrayList<RemoteAction>? = { null }