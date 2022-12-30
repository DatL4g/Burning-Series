package dev.datlag.burningseries.datastore

import androidx.datastore.core.Serializer
import dev.datlag.burningseries.datastore.preferences.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class AppSettingsSerializer : Serializer<AppSettings> {

    override val defaultValue: AppSettings
        get() = AppSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettings {
        return try {
            AppSettings.parseDelimitedFrom(input)
        } catch (ignored: Throwable) {
            try {
                AppSettings.parseFrom(input)
            } catch (ignored: Throwable) {
                try {
                    AppSettings.parseFrom(input.readBytes())
                } catch (ignored: Throwable) {
                    defaultValue
                }
            }
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) = withContext(Dispatchers.IO) {
        try {
            t.writeDelimitedTo(output)
        } catch (ignored: Throwable) {
            try {
                output.write(t.toByteArray())
            } catch (ignored: Throwable) { }
        }
    }
}