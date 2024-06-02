package dev.datlag.burningseries.ui.navigation.screen.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.ui.graphics.vector.ImageVector
import dev.datlag.burningseries.ui.custom.MaterialSymbols
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface NavigationBarState {

    @Transient
    val unselectedIcon: ImageVector

    @Transient
    val selectedIcon: ImageVector
        get() = unselectedIcon

    val sponsorIcon: ImageVector
        get() = when (this) {
            is Sponsor -> selectedIcon
            else -> Sponsor.unselectedIcon
        }

    val homeIcon: ImageVector
        get() = when (this) {
            is Home -> selectedIcon
            else -> Home.unselectedIcon
        }

    val favoriteIcon: ImageVector
        get() = when (this) {
            is Favorite -> selectedIcon
            else -> Favorite.unselectedIcon
        }

    @Serializable
    data object Sponsor : NavigationBarState {
        override val unselectedIcon: ImageVector
            get() = Icons.Rounded.Savings
    }

    @Serializable
    data object Home : NavigationBarState {
        override val unselectedIcon: ImageVector
            get() = MaterialSymbols.Rounded.Home

        override val selectedIcon: ImageVector
            get() = MaterialSymbols.Filled.Home
    }

    @Serializable
    data object Favorite : NavigationBarState {
        override val unselectedIcon: ImageVector
            get() = Icons.Rounded.FavoriteBorder

        override val selectedIcon: ImageVector
            get() = Icons.Rounded.Favorite
    }
}