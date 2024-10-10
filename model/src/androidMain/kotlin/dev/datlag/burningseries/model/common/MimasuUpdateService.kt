package dev.datlag.burningseries.model.common

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.datlag.mimasu.core.update.IUpdateCheckCallback
import dev.datlag.mimasu.core.update.IUpdateInfo
import dev.datlag.mimasu.core.update.IUpdateService
import kotlinx.coroutines.CoroutineScope

class MimasuUpdateService : LifecycleService() {

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)

        return Binder(lifecycleScope)
    }

    data class UpdateInfo(
        val available: Boolean,
        val required: Boolean,
        val playStore: String? = null,
        val directDownload: String? = null
    ) : IUpdateInfo.Stub() {
        override fun available(): Boolean {
            return available
        }

        override fun required(): Boolean {
            return required
        }

        override fun playstore(): String? {
            return playStore
        }

        override fun directDownload(): String? {
            return directDownload
        }
    }

    class Binder(
        val scope: CoroutineScope
    ) : IUpdateService.Stub() {
        override fun hasUpdate(callback: IUpdateCheckCallback?) {
            callback?.onUpdateInfo(
                UpdateInfo(
                    available = true,
                    required = true
                )
            )
        }
    }
}