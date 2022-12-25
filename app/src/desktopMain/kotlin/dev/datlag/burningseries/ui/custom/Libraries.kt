package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import dev.datlag.burningseries.LocalResources
import dev.datlag.burningseries.other.Resources

@Composable
actual fun Libraries(onLibraryClicked: (String?) -> Unit) {
    LibrariesContainer(aboutLibsJson = LocalResources.current.getResourcesAsInputStream(Resources.ABOUT_LIBRARIES)?.use {
        it.bufferedReader().readText()
    } ?: String(), modifier = Modifier.fillMaxSize(), onLibraryClick = {
        onLibraryClicked(it.scm?.url ?: it.website)
    })
}