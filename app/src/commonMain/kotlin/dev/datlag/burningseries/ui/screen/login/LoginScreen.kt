package dev.datlag.burningseries.ui.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    user: String,
    password: String,
    onUserChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: (user: String, password: String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = user,
            maxLines = 1,
            label = {
                Text(text = "Enter your username")
            },
            onValueChange = onUserChanged
        )
        Spacer(modifier = Modifier.padding(4.dp))
        TextField(
            value = password,
            maxLines = 1,
            label = {
                Text(text = "Enter your password")
            },
            onValueChange = onPasswordChanged
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Button(onClick = {
            onLoginClicked(user, password)
        }) {
            Text(text = "Login")
        }
    }
}