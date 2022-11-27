package dev.datlag.burningseries.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(component: LoginComponent) {
    val username by component.username.collectAsState(String())
    val password by component.password.collectAsState(String())
    var displayPassword by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = MaterialTheme.colorScheme.primaryContainer,
        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
        unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primaryContainer,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.primaryContainer,
        focusedLabelColor = MaterialTheme.colorScheme.primaryContainer,
        unfocusedLabelColor = MaterialTheme.colorScheme.primaryContainer
    )

    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = username,
                maxLines = 1,
                label = {
                    Text(text = "Enter your username")
                },
                onValueChange = {
                    scope.launch(Dispatchers.IO) {
                        component.username.emit(it)
                    }
                },
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.padding(4.dp))
            OutlinedTextField(
                value = password,
                maxLines = 1,
                label = {
                    Text(text = "Enter your password")
                },
                onValueChange = {
                    scope.launch(Dispatchers.IO) {
                        component.password.emit(it)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = {
                        displayPassword = !displayPassword
                    }) {
                        Icon(
                            imageVector = if (displayPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (displayPassword) LocalStringRes.current.hidePassword else LocalStringRes.current.showPassword
                        )
                    }
                },
                visualTransformation = if (displayPassword) VisualTransformation.None else PasswordVisualTransformation(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Button(onClick = {
                component.onLoginClicked()
            }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )) {
                Text(text = LocalStringRes.current.login)
            }
        }
    }
}