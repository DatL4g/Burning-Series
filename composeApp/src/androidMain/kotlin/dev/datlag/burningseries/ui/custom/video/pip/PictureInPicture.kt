package dev.datlag.burningseries.ui.custom.video.pip

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.media3.ui.PlayerView
import dev.datlag.burningseries.ui.custom.findActivity

internal fun enterPIPMode(context: Context, defaultPlayerView: PlayerView) {
    if (context.hasPIPFeature()) {
        defaultPlayerView.useController = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                params.setAspectRatio(Rational(19, 9))
                    .setSeamlessResizeEnabled(true)
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