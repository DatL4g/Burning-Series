package dev.datlag.burningseries.shared.ui.screen.initial.sponsor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.haze
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.LocalHaze
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.*
import dev.datlag.burningseries.shared.other.Constants
import dev.datlag.burningseries.shared.other.Project
import dev.datlag.burningseries.shared.ui.custom.ProjectCard
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SponsorScreen(component: SponsorComponent) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().haze(state = LocalHaze.current),
        contentPadding = LocalPadding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(SharedRes.strings.sponsor),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                text = stringResource(SharedRes.strings.sponsor_text)
            )
        }
        item {
            Text(
                text = stringResource(SharedRes.strings.amount_text)
            )
        }
        item {
            FlowRow(
                modifier = Modifier.fillParentMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val di = LocalDI.current

                Button(
                    onClick = {
                        Constants.Sponsor.GITHUB.openInBrowser(di)
                    },
                    colors = ButtonDefaults.githubColors()
                ) {
                    Image(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        painter = painterResource(SharedRes.images.GitHub),
                        contentDescription = stringResource(SharedRes.strings.github),
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(SharedRes.strings.github))
                }
                Button(
                    onClick = {
                        Constants.Sponsor.POLAR.openInBrowser(di)
                    },
                    colors = ButtonDefaults.polarColors()
                ) {
                    Image(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        painter = painterResource(SharedRes.images.Polar),
                        contentDescription = stringResource(SharedRes.strings.polar),
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(SharedRes.strings.polar))
                }
                Button(
                    onClick = {
                        Constants.Sponsor.PATREON.openInBrowser(di)
                    },
                    colors = ButtonDefaults.patreonColors()
                ) {
                    Image(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        painter = painterResource(SharedRes.images.Patreon),
                        contentDescription = stringResource(SharedRes.strings.patreon),
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(SharedRes.strings.patreon))
                }
                Button(
                    onClick = {
                        Constants.Sponsor.PAYPAL.openInBrowser(di)
                    },
                    colors = ButtonDefaults.paypalColors()
                ) {
                    Image(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        painter = painterResource(SharedRes.images.PayPal),
                        contentDescription = stringResource(SharedRes.strings.paypal),
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(SharedRes.strings.paypal))
                }
            }
        }
        item {
            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = stringResource(SharedRes.strings.free_support),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                text = stringResource(SharedRes.strings.free_support_text)
            )
        }
        item {
            ProjectCard(Project.PULZ, modifier = Modifier.fillParentMaxWidth())
        }
        item {
            ProjectCard(Project.AniFlow, modifier = Modifier.fillParentMaxWidth())
        }
    }
}