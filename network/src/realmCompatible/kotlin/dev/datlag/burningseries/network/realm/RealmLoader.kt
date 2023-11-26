package dev.datlag.burningseries.network.realm

import dev.datlag.burningseries.model.common.suspendCatching
import dev.datlag.burningseries.network.state.NetworkStateSaver
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import org.mongodb.kbson.BsonDocument
import io.realm.kotlin.mongodb.ext.call

actual class RealmLoader(private val app: App?) {
    actual suspend fun login() {
        if (mongoUser == null) {
            mongoUser = suspendCatching {
                app?.login(Credentials.anonymous())
            }.getOrNull()
        }
    }

    actual suspend fun loadEpisodes(hosterHref: List<String>): List<String> {
        return suspendCatching {
            val doc = mongoUser?.functions?.call<BsonDocument>("query", hosterHref.toTypedArray())
            doc?.getArray("result")?.values?.map { it.asDocument().getString("url").value }
        }.getOrNull() ?: emptyList()
    }

    companion object {
        private var mongoUser: User? = null
    }
}