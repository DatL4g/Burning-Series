package dev.datlag.burningseries.ui.navigation

interface K2KastComponent : Component {

    suspend fun k2kastLoad(href: String?)
}