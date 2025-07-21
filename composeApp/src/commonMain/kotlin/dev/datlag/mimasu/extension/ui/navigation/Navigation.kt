package dev.datlag.mimasu.extension.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.datlag.mimasu.extension.ui.navigation.aniworld.AniWorld
import dev.datlag.mimasu.extension.ui.navigation.burningseries.BurningSeries
import dev.datlag.mimasu.extension.ui.navigation.home.Home
import dev.datlag.mimasu.extension.ui.navigation.serienstream.SerienStream
import kotlinx.serialization.Serializable

object Navigation {

    @Serializable
    data object Home

    @Serializable
    data object BurningSeries

    @Serializable
    data object AniWorld

    @Serializable
    data object SerienStream
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
                },
                navigateToAniWorld = {
                    controller.navigate(Navigation.AniWorld)
                },
                navigateToSerienStream = {
                    controller.navigate(Navigation.SerienStream)
                }
            )
        }
        composable<Navigation.BurningSeries> {
            BurningSeries(
                onBack = { controller.navigateUp() }
            )
        }
        composable<Navigation.AniWorld> {
            AniWorld(
                onBack = { controller.navigateUp() }
            )
        }
        composable<Navigation.SerienStream> {
            SerienStream(
                onBack = { controller.navigateUp() }
            )
        }
    }
}