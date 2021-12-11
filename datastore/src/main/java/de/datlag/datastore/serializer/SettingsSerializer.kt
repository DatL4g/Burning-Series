package de.datlag.datastore.serializer

import androidx.datastore.core.Serializer
import de.datlag.datastore.SettingsPreferences
import io.michaelrocks.paranoid.Obfuscate
import java.io.InputStream
import java.io.OutputStream

@Obfuscate
class SettingsSerializer : Serializer<SettingsPreferences> {

    override val defaultValue: SettingsPreferences = SettingsPreferences.newBuilder()
        .setVideo(SettingsPreferences.Video.newBuilder()
            .setAdvancedFetching(false)
            .setPreferMp4(false))
        .build()

    override suspend fun readFrom(input: InputStream): SettingsPreferences {
        return try {
            return SettingsPreferences.parseFrom(input)
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SettingsPreferences, output: OutputStream) = t.writeTo(output)
}