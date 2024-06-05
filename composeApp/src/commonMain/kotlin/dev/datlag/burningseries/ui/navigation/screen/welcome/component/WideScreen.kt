package dev.datlag.burningseries.ui.navigation.screen.welcome.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalDarkMode
import dev.icerock.moko.resources.compose.painterResource
import dev.datlag.burningseries.MokoRes

@Composable
internal fun WideScreen(content: LazyListScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        Column(
            modifier = Modifier.weight(1F).safeDrawingPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val res = if (LocalDarkMode.current) {
                MokoRes.images.movie_dark
            } else {
                MokoRes.images.movie_light
            }

            Image(
                modifier = Modifier.fillMaxWidth(0.5F),
                painter = painterResource(res),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = WindowInsets.safeContent.asPaddingValues()
        ) {
            content()
        }
    }
}