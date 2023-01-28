package dev.datlag.burningseries.common

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getSafeParcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val result = runCatching {
            this.getParcelable(key, T::class.java)
        }
        if (result.isFailure) {
            this.getParcelable(key)
        } else {
            result.getOrNull()
        }
    } else {
        this.getParcelable(key)
    }
}