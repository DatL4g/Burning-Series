package dev.datlag.burningseries.shared.other

import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data object DomainVerifier {

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    val supported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val verified: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @Suppress("NewApi")
    fun verify(context: Context) {
        if (supported) {
            val manager = ContextCompat.getSystemService(context, DomainVerificationManager::class.java)
            val userState = manager?.getDomainVerificationUserState(context.packageName)
            val unapprovedDomains = userState?.hostToStateMap?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_NONE }

            unapprovedDomains?.let { count ->
                verified.update { count.isEmpty() }
            }
        }
    }

    @Suppress("NewApi")
    fun enable(context: Context) {
        if (supported) {
            val intent = Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                Uri.parse("package:${context.packageName}")
            )
            ContextCompat.startActivity(context, intent, null)
        }
    }
}