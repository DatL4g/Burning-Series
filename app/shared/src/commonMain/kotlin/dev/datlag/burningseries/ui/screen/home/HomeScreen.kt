package dev.datlag.burningseries.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(component: HomeComponent) {
    Column {
        Text(text = "Hello World!")
        Button(
            onClick = {

            }
        ) {
            Text(text = "Dummy")
        }
    }
}