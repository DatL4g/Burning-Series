package dev.datlag.burningseries.ui.custom.video.pip

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.media3.ui.PlayerView
import dev.datlag.burningseries.ui.custom.findActivity

internal fun enterPIPMode(context: Context) {
    if (context.hasPIPFeature()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var params = PictureInPictureParams.Builder()
                .setAspectRatio(
                    Rational(19, 9)
                )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                params = params.setSeamlessResizeEnabled(true).setAutoEnterEnabled(true)
            }

            context.findActivity()?.enterPictureInPictureMode(params.build())
        } else {
            context.findActivity()?.enterPictureInPictureMode()
        }
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
internal fun Context.hasPIPFeature(): Boolean {
    val pm = this.packageManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        pm.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                || if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             pm.hasSystemFeature(PackageManager.FEATURE_EXPANDED_PICTURE_IN_PICTURE)
        } else {
            false
        }
    } else {
        false
    }
}

internal fun Context.isActivityStatePipMode(): Boolean {
    val currentActivity = findActivity() ?: return false

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        currentActivity.isInPictureInPictureMode
    } else {
        false
    }
}