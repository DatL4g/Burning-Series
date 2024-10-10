package dev.datlag.burningseries.ui.navigation.screen.home.dialog.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson
import dev.datlag.burningseries.LocalEdgeToEdge
import dev.datlag.burningseries.common.merge
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.open_source_licenses
import dev.datlag.burningseries.composeapp.generated.resources.open_source_licenses_text
import dev.datlag.burningseries.ui.navigation.screen.home.dialog.about.component.LibraryCard
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.typography
import dev.datlag.tooling.compose.withIOContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun AboutDialog(component: AboutComponent) {
    val sheetState = rememberModalBottomSheetState()
    val (insets, bottomPadding) = if (LocalEdgeToEdge.current) {
        WindowInsets(
            left = 0,
            top = 0,
            right = 0,
            bottom = 0
        ) to BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom).asPaddingValues()
    } else {
        BottomSheetDefaults.windowInsets to PaddingValues()
    }

    ModalBottomSheet(
        onDismissRequest = component::dismiss,
        contentWindowInsets = { insets },
        sheetState = sheetState
    ) {
        val libs by produceState<Libs?>(null) {
            value = withIOContext {
                Libs.Builder().withJson(Res.readBytes("files/aboutlibraries.json")).build()
            }
        }
        val libsList = remember(libs) {
            libs?.libraries.orEmpty()
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = bottomPadding.merge(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    modifier = Modifier.fillParentMaxWidth(),
                    text = stringResource(Res.string.open_source_licenses),
                    style = Platform.typography().headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            item {
                Text(
                    modifier = Modifier.fillParentMaxWidth().padding(vertical = 16.dp),
                    text = stringResource(Res.string.open_source_licenses_text),
                    textAlign = TextAlign.Center
                )
            }
            items(libsList, key = { it.uniqueId }) {
                LibraryCard(it)
            }
        }
    }
}