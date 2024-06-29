package dev.datlag.burningseries.common

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.vanniktech.blurhash.BlurHash
import dev.datlag.tooling.Platform
import dev.datlag.tooling.async.scopeCatching
import dev.datlag.tooling.systemEnv
import dev.datlag.tooling.systemProperty

actual fun BlurHash.decode(
    hash: String?,
    width: Int,
    height: Int
): ImageBitmap? {
    if (hash.isNullOrBlank()) {
        return null
    }

    val bitmap = decode(
        blurHash = hash,
        width = width,
        height = height
    )
    return bitmap?.asImageBitmap()
}

@Composable
actual fun Modifier.drawProgress(color: Color, progress: Float): Modifier = drawWithContent {
    with(drawContext.canvas.nativeCanvas) {
        val checkPoint = saveLayer(null, null)

        drawContent()

        drawRect(
            color = color,
            size = Size(size.width * progress, size.height),
            blendMode = BlendMode.SrcOut
        )

        restoreToCount(checkPoint)
    }
}

@Composable
actual fun Platform.rememberIsTv(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        isTelevision(context)
    }
}

fun Platform.deviceName(context: Context): String {
    val settingsName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        scopeCatching {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        }.getOrNull()?.ifBlank { null }
    } else {
        null
    }

    if (!settingsName.isNullOrBlank()) {
        return settingsName
    }

    val bluetoothName = scopeCatching {
        Settings.Secure.getString(context.contentResolver, "bluetooth_name")
    }.getOrNull()?.ifBlank { null }

    if (!bluetoothName.isNullOrBlank()) {
        return bluetoothName
    }

    return systemEnv("HOSTNAME")?.ifBlank { null }
        ?: systemProperty("HOSTNAME")?.ifBlank { null }
        ?: Build.MODEL
}