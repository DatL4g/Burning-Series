package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.runtime.*
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permission(
    permission: String,
    onGranted: @Composable () -> Unit,
    onShowInfo: @Composable (PermissionState) -> Unit,
    onDeniedForever: @Composable (PermissionState) -> Unit
) {
    var callbackResult by remember { mutableStateOf<Boolean?>(null) }
    val permissionState = rememberPermissionState(
        permission
    ) { granted ->
        callbackResult = granted
    }

    if (permissionState.status.isGranted) {
        onGranted()
    } else {
        if (callbackResult == true) {
            SideEffect {
                // Re-request permission to make sure granted state will be reached
                permissionState.launchPermissionRequest()
            }
        } else {
            if (callbackResult == false && permissionState.status.shouldShowRationale) {
                onDeniedForever(permissionState)
            } else {
                onShowInfo(permissionState)
            }
        }
    }
}