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
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.movie_dark
import dev.datlag.burningseries.composeapp.generated.resources.movie_light
import dev.datlag.burningseries.ui.navigation.screen.welcome.WelcomeComponent
import org.jetbrains.compose.resources.painterResource

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
                Res.drawable.movie_dark
            } else {
                Res.drawable.movie_light
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