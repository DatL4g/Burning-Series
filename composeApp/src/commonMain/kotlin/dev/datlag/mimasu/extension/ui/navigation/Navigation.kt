package dev.datlag.mimasu.extension.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.datlag.mimasu.extension.ui.navigation.burningseries.BurningSeries
import dev.datlag.mimasu.extension.ui.navigation.home.Home
import kotlinx.serialization.Serializable

object Navigation {

    @Serializable
    data object Home

    @Serializable
    data object BurningSeries
}

@Composable
fun Navigation() {
    val controller = rememberNavController()

    NavHost(
        navController = controller,
        startDestination = Navigation.Home
    ) {
        composable<Navigation.Home> {
            Home(
                navigateToBurningSeries = {
                    controller.navigate(Navigation.BurningSeries)
                }
            )
        }
        composable<Navigation.BurningSeries> {
            BurningSeries(
                onBack = { controller.navigateUp() }
            )
        }
    }
}