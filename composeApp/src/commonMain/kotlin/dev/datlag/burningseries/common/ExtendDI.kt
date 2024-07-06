package dev.datlag.burningseries.common

import dev.datlag.burningseries.firebase.FirebaseFactory
import org.kodein.di.DIAware
import org.kodein.di.DirectDI
import org.kodein.di.instanceOrNull

fun DIAware.nullableFirebaseInstance(): FirebaseFactory? {
    val instance by this.instanceOrNull<FirebaseFactory>()
    return when (val state = instance) {
        is FirebaseFactory.Empty -> null
        else -> state
    }
}

fun DirectDI.nullableFirebaseInstance(): FirebaseFactory? {
    return when (val instance = instanceOrNull<FirebaseFactory>()) {
        is FirebaseFactory.Empty -> null
        else -> instance
    }
}