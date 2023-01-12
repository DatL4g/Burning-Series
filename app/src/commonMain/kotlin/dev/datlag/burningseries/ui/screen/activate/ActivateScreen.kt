package dev.datlag.burningseries.ui.screen.activate

import androidx.compose.material.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.Success
import dev.datlag.burningseries.ui.custom.WebView
import dev.datlag.burningseries.ui.Shape
import dev.datlag.burningseries.ui.dialog.save.SaveResultComponent
import dev.datlag.burningseries.ui.dialog.save.SaveResultDialog
import kotlinx.coroutines.launch

@Composable
fun ActivateScreen(component: ActivateComponent) {
    val dialogState = component.dialog.subscribeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        component.onGoBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = LocalStringRes.current.back,
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                },
                title = {
                    Text(
                        text = component.series.title,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                },
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                elevation = 0.dp
            )
        }
    ) {
        WebView(component)
    }

    dialogState.value.overlay?.also { (config, instance) ->
        when (config) {
            is DialogConfig.SaveResult -> {
                SaveResultDialog(instance as SaveResultComponent)
            }
        }
    }
}