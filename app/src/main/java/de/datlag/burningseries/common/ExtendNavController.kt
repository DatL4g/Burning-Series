package de.datlag.burningseries.common

import androidx.annotation.MainThread
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph
import timber.log.Timber

@MainThread
fun NavController.safeNavigate(directions: NavDirections) {
    fun tryNavigation() {
        try {
            navigate(directions)
        } catch (ignored: Throwable) {
            Timber.e(ignored)
        }
    }

    val destinationId = currentDestination?.getAction(directions.actionId)?.destinationId ?: 0
    currentDestination?.let { node ->
        val currentNode = when (node) {
            is NavGraph -> node
            else -> node.parent
        }
        if (destinationId != 0) {
            currentNode?.findNode(destinationId)?.let {
                navigate(directions.actionId, directions.arguments)
            } ?: tryNavigation()
        } else {
            tryNavigation()
        }
    } ?: tryNavigation()
}