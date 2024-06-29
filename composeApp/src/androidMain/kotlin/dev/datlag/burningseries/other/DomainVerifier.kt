package dev.datlag.burningseries.other

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

    val syncingEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val linksSupported: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun verify(context: Context) {
        if (supported) {
            val manager = ContextCompat.getSystemService(context, DomainVerificationManager::class.java)
            val userState = manager?.getDomainVerificationUserState(context.packageName)

            val syncDomain = userState?.hostToStateMap?.get(Constants.SYNCING_DOMAIN) ?: DomainVerificationUserState.DOMAIN_STATE_NONE
            syncingEnabled.update {
                syncDomain != DomainVerificationUserState.DOMAIN_STATE_NONE
            }

            val unapproved = userState?.hostToStateMap?.filterKeys {
                !it.equals(Constants.SYNCING_DOMAIN, ignoreCase = true)
            }?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_NONE }

            unapproved?.let { count ->
                linksSupported.update { count.isEmpty() }
            }
        }
    }

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