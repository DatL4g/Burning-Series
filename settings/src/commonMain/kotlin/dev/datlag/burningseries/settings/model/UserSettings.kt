package dev.datlag.burningseries.settings.model

import androidx.datastore.core.okio.OkioSerializer
import dev.datlag.tooling.async.suspendCatching
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import okio.BufferedSink
import okio.BufferedSource

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class UserSettings(
    @ProtoNumber(1) val github: GitHub = GitHub(
        idToken = null,
        accessToken = null,
        refreshToken = null
    )
) {
    @Serializable
    data class GitHub(
        @ProtoNumber(1) val idToken: String?,
        @ProtoNumber(2) val accessToken: String?,
        @ProtoNumber(3) val refreshToken: String?,
    )

    companion object SettingsSerializer : OkioSerializer<UserSettings> {
        override val defaultValue: UserSettings = UserSettings()


        private val protobuf = ProtoBuf {
            encodeDefaults = true
        }

        override suspend fun readFrom(source: BufferedSource): UserSettings {
            return suspendCatching {
                protobuf.decodeFromByteArray<UserSettings>(source.readByteArray())
            }.getOrNull() ?: defaultValue
        }

        override suspend fun writeTo(t: UserSettings, sink: BufferedSink) {
            val newSink = suspendCatching {
                sink.write(protobuf.encodeToByteArray(t))
            }.getOrNull() ?: sink

            suspendCatching {
                newSink.emit()
            }.getOrNull()
        }
    }
}
