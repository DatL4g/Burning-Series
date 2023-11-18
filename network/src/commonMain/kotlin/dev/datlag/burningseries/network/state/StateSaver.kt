package dev.datlag.burningseries.network.state

import dev.gitlive.firebase.auth.FirebaseUser
import io.realm.kotlin.mongodb.User

internal data object StateSaver {

    var mongoUser: User? = null
    var firebaseUser: FirebaseUser? = null
}