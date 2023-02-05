package dev.datlag.burningseries.ui.custom

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchAppBar(
    text: String,
    placeholder: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary
    ) {
        val (searchFocusRequester) = FocusRequester.createRefs()
        var requestedFocus by remember { mutableStateOf(false) }

        TextField(
            modifier = Modifier.fillMaxWidth().focusRequester(searchFocusRequester).onGloballyPositioned {
                if (!requestedFocus) {
                    searchFocusRequester.requestFocus()
                    requestedFocus = true
                }
            },
            value = text,
            onValueChange = {
                onTextChange(it)
            },
            placeholder = {
                Text(
                    modifier = Modifier.alpha(ContentAlpha.medium),
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            },
            singleLine = true,
            leadingIcon = {
                IconButton(onClick = {
                    onCloseClicked()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = LocalStringRes.current.close,
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (text.isNotEmpty()) {
                        onTextChange(String())
                    } else {
                        onCloseClicked()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = LocalStringRes.current.clear,
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                onSearchClicked(text)
            }),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colorScheme.onTertiary,
                containerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onTertiary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

sealed interface SearchAppBarState {
    object OPENED : SearchAppBarState
    object CLOSED : SearchAppBarState
}