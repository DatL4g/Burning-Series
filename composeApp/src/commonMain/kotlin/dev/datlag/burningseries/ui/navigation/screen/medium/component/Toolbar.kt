package dev.datlag.burningseries.ui.navigation.screen.medium.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.option.OptionDialog
import com.maxkeppeler.sheets.option.models.DisplayMode
import com.maxkeppeler.sheets.option.models.Option
import com.maxkeppeler.sheets.option.models.OptionBody
import com.maxkeppeler.sheets.option.models.OptionConfig
import com.maxkeppeler.sheets.option.models.OptionSelection
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.common.icon
import dev.datlag.burningseries.common.isConnectedOrConnecting
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.cast
import dev.datlag.burningseries.composeapp.generated.resources.casting_not_supported
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.navigation.screen.medium.MediumComponent
import dev.datlag.kast.ConnectionState
import dev.datlag.kast.DeviceType
import dev.datlag.kast.Kast
import dev.datlag.kast.UnselectReason
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
internal fun Toolbar(
    component: MediumComponent,
    series: Series?
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    component.back()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = null
                )
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                val seriesTitle by component.seriesTitle.collectAsStateWithLifecycle(component.seriesData.mainTitle)
                val seriesSubTitle by component.seriesSubTitle.collectAsStateWithLifecycle(component.seriesData.subTitle)

                Text(
                    text = seriesTitle,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                seriesSubTitle?.ifBlank { null }?.let { sub ->
                    Text(
                        text = sub,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        actions = {
            val kastDevices by Kast.allAvailableDevices.collectAsStateWithLifecycle()
            val kastState by Kast.connectionState.collectAsStateWithLifecycle()
            val kastDialog = rememberUseCaseState()
            val isFavorite by component.isFavorite.collectAsStateWithLifecycle()

            OptionDialog(
                state = kastDialog,
                selection = OptionSelection.Single(
                    options = kastDevices.map { device ->
                        Option(
                            icon = IconSource(
                                imageVector = when (device.type) {
                                    is DeviceType.TV -> Icons.Rounded.Tv
                                    is DeviceType.SPEAKER -> Icons.Rounded.Speaker
                                    else -> Icons.Rounded.Devices
                                }
                            ),
                            titleText = device.name,
                            selected = device.isSelected
                        )
                    },
                    onSelectOption = { option, _ ->
                        val device = kastDevices.toList()[option]

                        if (device.isSelected) {
                            Kast.unselect(UnselectReason.disconnected)
                        } else {
                            Kast.select(device)
                        }
                    }
                ),
                config = OptionConfig(
                    mode = DisplayMode.LIST
                ),
                header = Header.Default(
                    icon = IconSource(
                        imageVector = Icons.Rounded.Cast
                    ),
                    title = stringResource(Res.string.cast)
                ),
                body = if (Kast.isSupported) {
                    null
                } else {
                    OptionBody.Default(
                        bodyText = stringResource(Res.string.casting_not_supported)
                    )
                }
            )

            when (kastState) {
                is ConnectionState.CONNECTED -> {
                    IconButton(
                        onClick = {
                            Kast.unselect(UnselectReason.disconnected)
                        }
                    ) {
                        Icon(
                            imageVector = kastState.icon,
                            contentDescription = null
                        )
                    }
                }
                is ConnectionState.CONNECTING -> {
                    IconButton(
                        onClick = {
                            Kast.unselect(UnselectReason.disconnected)
                        }
                    ) {
                        Icon(
                            imageVector = kastState.icon,
                            contentDescription = null
                        )
                    }
                }
                else -> {
                    IconButton(
                        onClick = {
                            kastDialog.show()
                        }
                    ) {
                        Icon(
                            imageVector = kastState.icon,
                            contentDescription = null
                        )
                    }
                }
            }

            if (isFavorite) {
                IconButton(
                    onClick = {
                        if (series != null) {
                            component.unsetFavorite(series)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        if (series != null) {
                            component.setFavorite(series)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        modifier = Modifier.hazeChild(
            state = LocalHaze.current,
            style = HazeMaterials.thin(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ).fillMaxWidth()
    )
}