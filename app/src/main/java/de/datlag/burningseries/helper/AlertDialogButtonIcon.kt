package de.datlag.burningseries.helper

import android.graphics.drawable.Drawable
import androidx.core.graphics.BlendModeCompat

data class AlertDialogButtonIcon(
    var icon: Drawable?,
    var size: Int? = null,
    var gravity: Gravity = Gravity.LEFT,
    var useTextColor: Boolean = true,
    var useTextSize: Boolean = false,
    var blendMode: BlendModeCompat = BlendModeCompat.SRC_IN
) {
    sealed class Gravity {
        object LEFT : Gravity()
        object TOP : Gravity()
        object RIGHT : Gravity()
        object BOTTOM : Gravity()
    }
}
