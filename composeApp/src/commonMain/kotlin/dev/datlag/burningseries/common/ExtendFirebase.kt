package dev.datlag.burningseries.common

import dev.datlag.burningseries.firebase.FirebaseFactory
import dev.datlag.burningseries.model.common.name
import dev.datlag.burningseries.ui.navigation.Component

fun FirebaseFactory.Crashlytics.screen(value: Component) {
    this.customKey("Screen", value::class.name)
}