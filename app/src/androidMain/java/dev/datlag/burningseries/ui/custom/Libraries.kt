package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

@Composable
actual fun Libraries(onLibraryClicked: (String?) -> Unit) {
    LibrariesContainer(modifier = Modifier.fillMaxSize(), onLibraryClick = {
        onLibraryClicked(it.scm?.url ?: it.website)
    })
}