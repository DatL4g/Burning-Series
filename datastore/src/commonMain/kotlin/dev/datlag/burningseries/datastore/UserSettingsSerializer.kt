package dev.datlag.burningseries.datastore

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import dev.datlag.burningseries.datastore.preferences.UserSettings

class UserSettingsSerializer(
    private val cryptoManager: CryptoManager
) : Serializer<UserSettings> {

    override val defaultValue: UserSettings
        get() = UserSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserSettings {
        val decryptedBytes = cryptoManager.decrypt(input)
        return UserSettings.parseFrom(decryptedBytes)
    }

    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        cryptoManager.encrypt(t.toByteArray(), output)
    }
}