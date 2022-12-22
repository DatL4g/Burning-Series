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
import dev.datlag.burningseries.common.Success
import dev.datlag.burningseries.ui.custom.WebView
import dev.datlag.burningseries.ui.Shape
import kotlinx.coroutines.launch

@Composable
fun ActivateScreen(component: ActivateComponent) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState)
    val success by component.saveSuccess.collectAsState(null)
    val (background, content) = if (success == null) {
        SnackbarDefaults.backgroundColor to androidx.compose.material.MaterialTheme.colors.surface
    } else {
        if (success!!) {
            Color.Success to Color.White
        } else {
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        component.onGoBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Text(text = component.series.title)
                },
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                elevation = 0.dp
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    snackbarData = data,
                    backgroundColor = background,
                    contentColor = content,
                    shape = Shape.FullRoundedShape,
                    elevation = 0.dp,
                    actionOnNewLine = false
                )
            }
        }
    ) {
        WebView(component)
    }

    if (success != null) {
        scope.launch {
            snackbarHostState.showSnackbar(message = if (success!!) "Saved Stream" else "Error saving stream")
        }
    }
}