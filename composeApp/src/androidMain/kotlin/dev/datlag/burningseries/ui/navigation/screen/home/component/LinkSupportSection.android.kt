package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.enable
import dev.datlag.burningseries.composeapp.generated.resources.open_domains_text_1
import dev.datlag.burningseries.composeapp.generated.resources.open_domains_text_2
import dev.datlag.burningseries.composeapp.generated.resources.open_domains_text_3
import dev.datlag.burningseries.composeapp.generated.resources.open_domains_title
import dev.datlag.burningseries.other.DomainVerifier
import dev.datlag.tooling.Platform
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

actual val Platform.linksSupported: StateFlow<Boolean>
    get() = DomainVerifier.linksSupported

actual fun LazyListScope.LinkSupportSection(
    headerPadding: PaddingValues
) {
    if (DomainVerifier.supported) {
        item {
            Text(
                modifier = Modifier.padding(headerPadding),
                text = stringResource(Res.string.open_domains_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Column(
                modifier = Modifier.fillParentMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                val context = LocalContext.current

                SideEffect {
                    DomainVerifier.verify(context)
                }

                Text(
                    text = buildString {
                        append(stringResource(Res.string.open_domains_text_1))
                        appendLine()
                        append(stringResource(Res.string.open_domains_text_2))
                        appendLine()
                        append(stringResource(Res.string.open_domains_text_3))
                    }
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        DomainVerifier.enable(context)
                    }
                ) {
                    Text(text = stringResource(Res.string.enable))
                }
            }
        }
    }
}