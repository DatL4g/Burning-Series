package dev.datlag.burningseries.ui.navigation.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.app_name
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.tooling.compose.ifFalse
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min
import dev.datlag.burningseries.MokoRes
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun CollapsingToolbar(
    state: TopAppBarState,
    scrollBehavior: TopAppBarScrollBehavior,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        val isCollapsed by remember(state) {
            derivedStateOf { state.collapsedFraction >= 0.99F }
        }
        val imageAlpha by remember(state) {
            derivedStateOf {
                max(min(1F - state.collapsedFraction, 1F), 0F)
            }
        }
        val girlAlpha = remember(isCollapsed, imageAlpha) {
            if (isCollapsed) 0F else min(imageAlpha * 2F, 1F)
        }

        Image(
            modifier = Modifier.fillMaxWidth().matchParentSize(),
            painter = painterResource(MokoRes.images.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            alpha = imageAlpha
        )

        Image(
            modifier = Modifier.fillMaxWidth().matchParentSize(),
            painter = painterResource(MokoRes.images.lighting),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopStart,
            alpha = imageAlpha
        )

        Image(
            modifier = Modifier.matchParentSize(),
            painter = painterResource(MokoRes.images.title),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Inside,
            alpha = girlAlpha
        )

        Image(
            modifier = Modifier.matchParentSize(),
            painter = painterResource(MokoRes.images.girl_floating),
            contentDescription = null,
            alignment = Alignment.BottomEnd,
            alpha = girlAlpha
        )

        LargeTopAppBar(
            navigationIcon = {
                IconButton(
                    modifier = Modifier.ifFalse(isCollapsed) {
                        background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75F), CircleShape)
                    },
                    onClick = {
                        onSettingsClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null
                    )
                }
            },
            title = {
                AnimatedVisibility(
                    visible = isCollapsed
                ) {
                    Text(
                        text = stringResource(Res.string.app_name)
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
            modifier = Modifier.hazeChild(
                state = LocalHaze.current,
                style = HazeMaterials.thin(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            ).fillMaxWidth()
        )
    }
}