package dev.datlag.burningseries.ui.navigation.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.home
import dev.datlag.burningseries.composeapp.generated.resources.sponsor
import dev.datlag.burningseries.composeapp.generated.resources.favorite
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun HidingNavigationBar(
    visible: Boolean,
    selected: NavigationBarState,
    onSponsor: () -> Unit,
    onHome: () -> Unit,
    onFavorite: () -> Unit,
) {
    val density = LocalDensity.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = {
                with(density) { it.dp.roundToPx() }
            },
            animationSpec = tween(
                easing = LinearOutSlowInEasing,
                durationMillis = 500
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                easing = LinearOutSlowInEasing,
                durationMillis = 500
            )
        )
    ) {
        NavigationBar(
            modifier = Modifier.hazeChild(
                state = LocalHaze.current,
                style = HazeMaterials.thin(NavigationBarDefaults.containerColor)
            ).fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.contentColorFor(NavigationBarDefaults.containerColor)
        ) {
            val isSponsor = remember(selected) {
                selected is NavigationBarState.Sponsor
            }
            val isHome = remember(selected) {
                selected is NavigationBarState.Home
            }
            val isFavorite = remember(selected) {
                selected is NavigationBarState.Favorite
            }

            NavigationBarItem(
                onClick = {
                    if (!isSponsor) {
                        onSponsor()
                    }
                },
                selected = isSponsor,
                icon = {
                    Icon(
                        imageVector = selected.sponsorIcon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = stringResource(Res.string.sponsor))
                }
            )
            NavigationBarItem(
                onClick = {
                    if (!isHome) {
                        onHome()
                    }
                },
                selected = isHome,
                icon = {
                    Icon(
                        imageVector = selected.homeIcon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = stringResource(Res.string.home))
                }
            )
            NavigationBarItem(
                onClick = {
                    if (!isFavorite) {
                        onFavorite()
                    }
                },
                selected = isFavorite,
                icon = {
                    Icon(
                        imageVector = selected.favoriteIcon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = stringResource(Res.string.favorite))
                }
            )
        }
    }
}