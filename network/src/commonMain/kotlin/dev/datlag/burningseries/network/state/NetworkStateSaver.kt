package dev.datlag.burningseries.network.state

import dev.gitlive.firebase.auth.FirebaseUser

data object NetworkStateSaver {

    var firebaseUser: FirebaseUser? = null

    val mongoHosterMap: MutableMap<String, List<String>> = mutableMapOf()
}