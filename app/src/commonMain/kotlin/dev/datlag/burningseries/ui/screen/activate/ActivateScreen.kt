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
import dev.datlag.burningseries.common.OnWarning
import dev.datlag.burningseries.common.Success
import dev.datlag.burningseries.common.Warning
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.ui.custom.WebView
import dev.datlag.burningseries.ui.Shape
import dev.datlag.burningseries.ui.custom.snackbarHandlerForStatus
import dev.datlag.burningseries.ui.dialog.save.SaveResultComponent
import dev.datlag.burningseries.ui.dialog.save.SaveResultDialog
import kotlinx.coroutines.launch
import androidx.compose.material.SnackbarDefaults
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ActivateScreen(component: ActivateComponent) {
    val dialogState = component.dialog.subscribeAsState()
    val strings = LocalStringRes.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scaffoldState = rememberScaffoldState(
        snackbarHostState = snackbarHostState
    )

    val colorScheme = MaterialTheme.colorScheme
    val defaultColors = SnackbarDefaults.backgroundColor to androidx.compose.material.MaterialTheme.colors.surface
    var snackbarColors by remember { mutableStateOf(defaultColors) }

    snackbarHandlerForStatus(
        state = snackbarHostState,
        status = component.status,
        mapper = {
            when (it) {
                is Status.LOADING -> strings.loadingUrl
                is Status.ERROR -> strings.errorTryAgain
                else -> null
            }
        }
    ) { status ->
        snackbarColors = when (status) {
            is Status.LOADING -> Color.Warning to Color.OnWarning
            is Status.ERROR -> colorScheme.error to colorScheme.onError
            else -> defaultColors
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
                            contentDescription = LocalStringRes.current.back,
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                },
                title = {
                    Text(
                        text = component.series.title,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onTertiary,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
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
                    backgroundColor = snackbarColors.first,
                    contentColor = snackbarColors.second,
                    shape = Shape.FullRoundedShape,
                    elevation = 0.dp,
                    actionOnNewLine = false
                )
            }
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