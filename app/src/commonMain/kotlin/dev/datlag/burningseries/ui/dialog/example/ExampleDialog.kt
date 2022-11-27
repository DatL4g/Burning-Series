package dev.datlag.burningseries.ui.dialog.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.datlag.burningseries.ui.dialog.DialogComponent

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExampleDialog(component: DialogComponent) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0F, 0F, 0F, 0.5F)) {
        AlertDialog(
            onDismissRequest = {
                component.onDismissClicked()
            },
            title = {
                Text("Example Dialog")
            },
            text = {
                Text(component.message)
            },
            confirmButton = {
                TextButton(onClick = {
                    component.onDismissClicked()
                }) {
                    Text("Dismiss")
                }
            }
        )
    }
}