package dev.datlag.burningseries.other

import dev.datlag.burningseries.settings.Settings
import dev.datlag.burningseries.settings.model.AppSettings
import dev.datlag.burningseries.settings.model.UserSettings
import dev.datlag.tooling.async.suspendCatching
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
class SyncHelper(
    private val appSettings: Settings.PlatformAppSettings,
    private val userSettings: Settings.PlatformUserSettings
) {

    suspend fun encodeSettingsToByteArray(): ByteArray {
        val app = appSettings.all.firstOrNull() ?: return ByteArray(0)
        val user = userSettings.all.firstOrNull() ?: return ByteArray(0)

        val data = SyncSettings(app, user)
        return protoBuf.encodeToByteArray<SyncSettings>(data)
    }

    suspend fun updateSettingsFromByteArray(data: ByteArray): Boolean {
        if (data.isEmpty()) {
            return false
        }

        val syncSettings = suspendCatching {
            protoBuf.decodeFromByteArray<SyncSettings>(data)
        }.getOrNull() ?: return false

        appSettings.updateAll(syncSettings.appSettings)
        userSettings.updateAll(syncSettings.userSettings)
        return true
    }

    @Serializable
    data class SyncSettings(
        @ProtoNumber(1) val appSettings: AppSettings,
        @ProtoNumber(2) val userSettings: UserSettings
    )

    companion object {
        private val protoBuf = ProtoBuf {
            encodeDefaults = true
        }
    }
}