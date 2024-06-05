package dev.datlag.burningseries.ui.navigation.screen.welcome.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalDarkMode
import dev.datlag.burningseries.ui.navigation.screen.welcome.WelcomeComponent
import dev.icerock.moko.resources.compose.painterResource
import dev.datlag.burningseries.MokoRes

@Composable
internal fun CompactScreen(content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = WindowInsets.safeContent.asPaddingValues()
    ) {
        item {
            val res = if (LocalDarkMode.current) {
                MokoRes.images.movie_dark
            } else {
                MokoRes.images.movie_light
            }

            Image(
                modifier = Modifier.fillParentMaxWidth(0.5F),
                painter = painterResource(res),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
        content()
    }
}