package de.datlag.burningseries.helper

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import de.datlag.burningseries.common.safeActivity
import de.datlag.burningseries.common.safeContext
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
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
        constructor(fragment: Fragment) : this(fragment.safeContext, fragment.safeActivity)

        private val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        private var theme: Theme? = if (activity == null) null else Theme(activity)

        fun getMode(): Mode {
            val delegateMode = getDelegateMode()
            val uiMode = getUiMode()

            return if (delegateMode == Mode.LIGHT || uiMode == Mode.LIGHT) {
                Mode.LIGHT
            } else if (delegateMode == Mode.DARK || uiMode == Mode.DARK) {
                Mode.DARK
            } else {
                Mode.SYSTEM
            }
        }

        fun getModePreferDelegate(): Mode {
            val delegateMode = getDelegateMode()
            val uiMode = getUiMode()

            return if (
                delegateMode == Mode.LIGHT
                || (delegateMode == Mode.SYSTEM && uiMode == Mode.LIGHT)
            ) {
                Mode.LIGHT
            } else if (
                delegateMode == Mode.DARK
                || (delegateMode == Mode.SYSTEM && uiMode == Mode.DARK)
            ) {
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

        fun getDelegateMode(): Mode {
            return when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_NO -> Mode.LIGHT
                AppCompatDelegate.MODE_NIGHT_YES -> Mode.DARK
                else -> Mode.SYSTEM
            }
        }
    }

    sealed class Mode {
        object LIGHT : Mode()
        object DARK : Mode()
        object SYSTEM : Mode()

        fun toDelegateMode(): Int {
            return when (this) {
                is LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                is DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }

        fun isDarkMode(): Boolean {
            return this is DARK
        }
    }
}