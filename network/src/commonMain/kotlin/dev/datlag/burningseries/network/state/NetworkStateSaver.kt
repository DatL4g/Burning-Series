package dev.datlag.burningseries.network.state

import dev.gitlive.firebase.auth.FirebaseUser
import io.realm.kotlin.mongodb.User

data object NetworkStateSaver {

    var mongoUser: User? = null
    var firebaseUser: FirebaseUser? = null

    val mongoHosterMap: MutableMap<String, List<String>> = mutableMapOf()
}