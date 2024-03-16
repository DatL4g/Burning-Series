package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.withIOContext
import kotlinx.coroutines.delay

@Composable
fun FloatingSearchButton(
    icon: ImageVector = Icons.Default.Search,
    contentDescription: String? = stringResource(SharedRes.strings.search),
    clearIcon: ImageVector = Icons.Default.Clear,
    closeIcon: ImageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
    modifier: Modifier = Modifier,
    onTextChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var opened by remember { mutableStateOf(false) }
    val textState = remember { mutableStateOf("") }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier,
        shape = FloatingActionButtonDefaults.shape,
        shadowElevation = 6.dp,
        onClick = {
            if (!opened) {
                opened = true
            } else {
                focusRequester.requestFocus()
            }
        }
    ) {
        AnimatedContent(targetState = opened) { expand ->
            if (expand) {
                SearchBar(
                    close = {
                        opened = false
                    },
                    clearIcon = clearIcon,
                    closeIcon = closeIcon,
                    focusRequester = focusRequester,
                    textState = textState,
                    onTextChange = onTextChange
                )

                LaunchedEffect(focusRequester) {
                    withIOContext {
                        delay(500) // wait till transition is done
                    }
                    focusRequester.requestFocus()
                }
            } else {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    focusRequester: FocusRequester,
    textState: MutableState<String>,
    close: () -> Unit,
    clearIcon: ImageVector,
    closeIcon: ImageVector,
    onTextChange: (String) -> Unit
) {
    TextField(
        value = textState.value,
        onValueChange = {
            textState.value = it
            onTextChange(textState.value)
        },
        modifier = Modifier.focusRequester(focusRequester),
        placeholder = {
            Text(
                text = stringResource(SharedRes.strings.search),
                style = MaterialTheme.typography.labelLarge
            )
        },
        singleLine = true,
        colors = searchTextFieldColors(),
        keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search),
        leadingIcon = {
            IconButton(
                onClick = close
            ) {
                Icon(
                    imageVector = closeIcon,
                    contentDescription = stringResource(SharedRes.strings.close)
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    textState.value = ""
                    onTextChange(textState.value)
                }
            ) {
                Icon(
                    imageVector = clearIcon,
                    contentDescription = stringResource(SharedRes.strings.clear)
                )
            }
        }
    )
}

@Composable
private fun searchTextFieldColors(
    contentColor: Color = LocalContentColor.current
): TextFieldColors {
    return TextFieldDefaults.colors(
        unfocusedTextColor = contentColor,
        focusedTextColor = contentColor,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        cursorColor = contentColor,
        selectionColors = TextSelectionColors(
            handleColor = contentColor,
            backgroundColor = contentColor.copy(alpha = 0.3f)
        ),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedLeadingIconColor = contentColor,
        unfocusedLeadingIconColor = contentColor,
        focusedTrailingIconColor = contentColor,
        unfocusedTrailingIconColor = contentColor,
        unfocusedPlaceholderColor = contentColor.copy(alpha = 0.5F),
        focusedPlaceholderColor = contentColor.copy(alpha = 0.5F),
    )
}
