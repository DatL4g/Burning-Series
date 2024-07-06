package dev.datlag.burningseries.other

import android.content.Context
import androidx.media3.cast.DefaultCastOptionsProvider
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

@UnstableApi
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(p0: Context): CastOptions {
        return CastOptions.Builder()
            .setResumeSavedSession(false)
            .setStopReceiverApplicationWhenEndingSession(true)
            .setEnableReconnectionService(true)
            .setRemoteToLocalEnabled(true)
            .setReceiverApplicationId(DefaultCastOptionsProvider.APP_ID_DEFAULT_RECEIVER_WITH_DRM)
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context): MutableList<SessionProvider>? {
        return null
    }
}