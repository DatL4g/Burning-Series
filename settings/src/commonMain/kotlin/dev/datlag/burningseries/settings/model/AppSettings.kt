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
data class AppSettings(
    @ProtoNumber(1) val language: Language?,
    @ProtoNumber(2) val customFonts: Boolean = true
) {
    companion object SettingsSerializer : OkioSerializer<AppSettings> {
        override val defaultValue: AppSettings = AppSettings(
            language = null,
        )

        private val protobuf = ProtoBuf {
            encodeDefaults = true
        }

        override suspend fun readFrom(source: BufferedSource): AppSettings {
            return suspendCatching {
                protobuf.decodeFromByteArray<AppSettings>(source.readByteArray())
            }.getOrNull() ?: defaultValue
        }

        override suspend fun writeTo(t: AppSettings, sink: BufferedSink) {
            val newSink = suspendCatching {
                sink.write(protobuf.encodeToByteArray(t))
            }.getOrNull() ?: sink

            suspendCatching {
                newSink.emit()
            }.getOrNull()
        }
    }
}
