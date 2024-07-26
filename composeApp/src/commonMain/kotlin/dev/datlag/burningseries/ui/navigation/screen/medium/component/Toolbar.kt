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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import dev.datlag.burningseries.common.rememberIsTv
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.cast
import dev.datlag.burningseries.composeapp.generated.resources.casting_not_supported
import dev.datlag.burningseries.composeapp.generated.resources.k2kast_connection_code
import dev.datlag.burningseries.composeapp.generated.resources.search
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.other.K2Kast
import dev.datlag.burningseries.ui.navigation.screen.medium.MediumComponent
import dev.datlag.kast.ConnectionState
import dev.datlag.kast.DeviceType
import dev.datlag.kast.Kast
import dev.datlag.kast.UnselectReason
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformIcon
import dev.datlag.tooling.compose.platform.PlatformIconButton
import dev.datlag.tooling.compose.platform.PlatformText
import dev.datlag.tooling.compose.platform.ProvideNonTvContentColor
import dev.datlag.tooling.compose.platform.ProvideNonTvTextStyle
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.platform.shapes
import dev.datlag.tooling.compose.platform.typography
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import dev.datlag.tooling.safeSubString
import dev.datlag.tooling.setFrom
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
internal fun Toolbar(
    component: MediumComponent,
    series: Series?
) {
    TopAppBar(
        navigationIcon = {
            ProvideNonTvContentColor {
                PlatformIconButton(
                    modifier = Modifier.focusProperties {
                        down = component.focus.seasonAndLanguageButtons
                    },
                    onClick = component::back
                ) {
                    PlatformIcon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = null
                    )
                }
            }
        },
        title = {
            ProvideNonTvContentColor {
                ProvideNonTvTextStyle {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                    ) {
                        val seriesTitle by component.seriesTitle.collectAsStateWithLifecycle(component.seriesData.mainTitle)
                        val seriesSubTitle by component.seriesSubTitle.collectAsStateWithLifecycle(component.seriesData.subTitle)

                        PlatformText(
                            text = seriesTitle,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        seriesSubTitle?.ifBlank { null }?.let { sub ->
                            PlatformText(
                                text = sub,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = Platform.typography().labelMedium
                            )
                        }
                    }
                }
            }
        },
        actions = {
            val isFavorite by component.isFavorite.collectAsStateWithLifecycle()

            if (!Platform.rememberIsTv()) {
                val kastDevices by Kast.allAvailableDevices.collectAsStateWithLifecycle()
                val k2kastDevices by K2Kast.devices.collectAsStateWithLifecycle()
                val combinedDevices = remember(kastDevices, k2kastDevices) {
                    setFrom(
                        k2kastDevices.map(MediumComponent.Device::K2K),
                        kastDevices.map(MediumComponent.Device::Chrome)
                    )
                }

                val kastState by Kast.connectionState.collectAsStateWithLifecycle()
                val k2kastConnected = remember(k2kastDevices) { k2kastDevices.any { it.selected } }
                val kastDialog = rememberUseCaseState()

                OptionDialog(
                    state = kastDialog,
                    selection = OptionSelection.Single(
                        options = combinedDevices.map { device ->
                            Option(
                                icon = IconSource(
                                    imageVector = device.icon
                                ),
                                titleText = device.name,
                                selected = device.selected
                            )
                        },
                        onSelectOption = { option, _ ->
                            val device = combinedDevices.toList()[option]

                            device.select()
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
                    body = OptionBody.Custom {
                        var value by remember { mutableStateOf("") }

                        LaunchedEffect(value) {
                            K2Kast.search(value)
                        }

                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = value,
                                onValueChange = {
                                    value = it.replace("\\D*".toRegex(), "").trim().safeSubString(0, 6)
                                },
                                placeholder = {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = stringResource(Res.string.k2kast_connection_code),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                shape = Platform.shapes().medium,
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Search
                                ),
                                singleLine = true,
                                maxLines = 1
                            )

                            if (!Kast.isSupported) {
                                Text(
                                    text = stringResource(Res.string.casting_not_supported),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                )

                when {
                    kastState is ConnectionState.CONNECTING || kastState is ConnectionState.CONNECTED -> {
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
                    k2kastConnected -> {
                        IconButton(
                            onClick = {
                                K2Kast.disconnect()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CastConnected,
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
            }

            ProvideNonTvContentColor {
                if (isFavorite) {
                    PlatformIconButton(
                        modifier = Modifier.focusProperties {
                            end = component.focus.floatingActionButton
                            down = component.focus.seasonAndLanguageButtons
                        },
                        onClick = {
                            if (series != null) {
                                component.unsetFavorite(series)
                            }
                        }
                    ) {
                        PlatformIcon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                } else {
                    PlatformIconButton(
                        modifier = Modifier.focusProperties {
                            end = component.focus.floatingActionButton
                            down = component.focus.seasonAndLanguageButtons
                        },
                        onClick = {
                            if (series != null) {
                                component.setFavorite(series)
                            }
                        }
                    ) {
                        PlatformIcon(
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                        )
                    }
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
                containerColor = Platform.colorScheme().surface
            )
        ).fillMaxWidth()
    )
}