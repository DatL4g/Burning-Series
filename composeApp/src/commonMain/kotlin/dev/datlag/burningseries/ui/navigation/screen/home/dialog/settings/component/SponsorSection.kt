package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.option.OptionDialog
import com.maxkeppeler.sheets.option.models.DisplayMode
import com.maxkeppeler.sheets.option.models.Option
import com.maxkeppeler.sheets.option.models.OptionConfig
import com.maxkeppeler.sheets.option.models.OptionSelection
import dev.icerock.moko.resources.compose.painterResource
import dev.datlag.burningseries.MokoRes
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.github
import dev.datlag.burningseries.composeapp.generated.resources.patreon
import dev.datlag.burningseries.composeapp.generated.resources.polar
import dev.datlag.burningseries.composeapp.generated.resources.sponsor
import dev.datlag.burningseries.other.Constants
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.onClick
import dev.datlag.tooling.compose.platform.shapes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorSection(
    modifier: Modifier = Modifier,
) {
    val sponsorDialog = rememberUseCaseState()
    val uriHandler = LocalUriHandler.current

    OptionDialog(
        state = sponsorDialog,
        config = OptionConfig(
            mode = DisplayMode.LIST
        ),
        selection = OptionSelection.Single(
            options = listOf(
                Option(
                    icon = IconSource(painter = painterResource(MokoRes.images.github)),
                    titleText = stringResource(Res.string.github)
                ),
                Option(
                    icon = IconSource(painter = painterResource(MokoRes.images.polar)),
                    titleText = stringResource(Res.string.polar)
                ),
                Option(
                    icon = IconSource(painter = painterResource(MokoRes.images.patreon)),
                    titleText = stringResource(Res.string.patreon)
                )
            ),
            onSelectOption = { option, _ ->
                when (option) {
                    0 -> uriHandler.openUri(Constants.Sponsor.GITHUB)
                    1 -> uriHandler.openUri(Constants.Sponsor.POLAR)
                    2 -> uriHandler.openUri(Constants.Sponsor.PATREON)
                }
            }
        ),
        header = Header.Default(
            title = stringResource(Res.string.sponsor),
            icon = IconSource(imageVector = Icons.Rounded.Savings)
        )
    )

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = ButtonDefaults.MinHeight)
            .clip(Platform.shapes().medium)
            .onClick {
                sponsorDialog.show()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Savings,
            contentDescription = null,
        )
        Text(text = stringResource(Res.string.sponsor))
    }
}