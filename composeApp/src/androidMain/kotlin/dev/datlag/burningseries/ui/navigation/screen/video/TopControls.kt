package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Devices
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.option.OptionDialog
import com.maxkeppeler.sheets.option.models.DisplayMode
import com.maxkeppeler.sheets.option.models.Option
import com.maxkeppeler.sheets.option.models.OptionConfig
import com.maxkeppeler.sheets.option.models.OptionSelection
import dev.datlag.burningseries.common.icon
import dev.datlag.burningseries.common.rememberIsTv
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.cast
import dev.datlag.burningseries.other.K2Kast
import dev.datlag.kast.ConnectionState
import dev.datlag.kast.DeviceType
import dev.datlag.kast.Kast
import dev.datlag.kast.UnselectReason
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformIcon
import dev.datlag.tooling.compose.platform.PlatformIconButton
import dev.datlag.tooling.compose.platform.PlatformText
import dev.datlag.tooling.compose.platform.ProvideNonTvContentColor
import dev.datlag.tooling.compose.platform.typography
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopControls(
    isVisible: Boolean,
    mainTitle: String,
    subTitle: String?,
    playerWrapper: PlayerWrapper,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val isFinished by playerWrapper.isFinished.collectAsStateWithLifecycle()

    AnimatedVisibility(
        modifier = modifier.safeDrawingPadding(),
        visible = isVisible || isFinished,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.5F)))
            TopAppBar(
                navigationIcon = {
                    ProvideNonTvContentColor {
                        PlatformIconButton(
                            onClick = onBack
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
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                        ) {
                            PlatformText(
                                text = mainTitle,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            subTitle?.ifBlank { null }?.let { sub ->
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
                },
                actions = {
                    if (!Platform.rememberIsTv()) {
                        val connectionState by Kast.connectionState.collectAsStateWithLifecycle()
                        val allAvailableDevices by Kast.allAvailableDevices.collectAsStateWithLifecycle()
                        val connectDialog = rememberUseCaseState(
                            onCloseRequest = {
                                playerWrapper.showControls()
                            },
                            onDismissRequest = {
                                Kast.Android.passiveDiscovery()
                                playerWrapper.showControls()
                            },
                            onFinishedRequest = {
                                playerWrapper.showControls()
                            }
                        )

                        OptionDialog(
                            state = connectDialog,
                            selection = OptionSelection.Single(
                                options = allAvailableDevices.map { device ->
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
                                    val device = allAvailableDevices.toList()[option]

                                    if (device.isSelected) {
                                        Kast.unselect(UnselectReason.disconnected)
                                        Kast.Android.passiveDiscovery()
                                    } else {
                                        K2Kast.disconnect()
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
                            )
                        )

                        when (connectionState) {
                            is ConnectionState.CONNECTED, is ConnectionState.CONNECTING -> {
                                IconButton(
                                    onClick = {
                                        Kast.unselect(UnselectReason.disconnected)
                                    }
                                ) {
                                    Icon(
                                        imageVector = connectionState.icon,
                                        contentDescription = null
                                    )
                                }
                            }
                            is ConnectionState.DISCONNECTED -> {
                                IconButton(
                                    onClick = {
                                        Kast.Android.activeDiscovery()
                                        connectDialog.show()
                                        playerWrapper.showControlsFor5Min()
                                    }
                                ) {
                                    Icon(
                                        imageVector = connectionState.icon,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    }
}