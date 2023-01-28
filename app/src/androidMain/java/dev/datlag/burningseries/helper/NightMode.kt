package dev.datlag.burningseries.helper

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object NightMode {

    class Theme(activity: Activity) {
        init {
            val currentMode = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
            if (sUiNightMode == Configuration.UI_MODE_NIGHT_UNDEFINED) {
                sUiNightMode = currentMode
            }
        }

        fun getConfigMode(): Int = sUiNightMode

        companion object {
            private var sUiNightMode = Configuration.UI_MODE_NIGHT_UNDEFINED
        }
    }

    class Helper(private val context: Context, activity: Activity? = null) {
        constructor(activity: Activity) : this(activity, activity)

        private val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        private var theme: Theme? = if (activity == null) null else Theme(activity)

        fun getMode(): Mode {
            val uiMode = getUiMode()

            return if (uiMode == Mode.LIGHT) {
                Mode.LIGHT
            } else if (uiMode == Mode.DARK) {
                Mode.DARK
            } else {
                Mode.SYSTEM
            }
        }

        fun getUiMode(): Mode {
            return if (
                uiModeManager.nightMode == UiModeManager.MODE_NIGHT_NO
                || theme?.getConfigMode() == Configuration.UI_MODE_NIGHT_NO
            ) {
                Mode.LIGHT
            } else if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES
                || theme?.getConfigMode() == Configuration.UI_MODE_NIGHT_YES
            ) {
                Mode.DARK
            } else {
                Mode.SYSTEM
            }
        }
    }

    sealed class Mode(val value: Int) {
        object LIGHT : Mode(1)
        object DARK : Mode(2)
        object SYSTEM : Mode(0)
    }
}