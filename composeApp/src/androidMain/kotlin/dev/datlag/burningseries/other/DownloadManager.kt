package dev.datlag.burningseries.other

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dev.datlag.burningseries.github.model.Asset
import dev.datlag.burningseries.github.model.UserAndRelease
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.compose.launchIO
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.createAsFileSafely
import dev.datlag.tooling.deleteSafely
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import okhttp3.internal.closeQuietly
import okio.Path.Companion.toOkioPath
import ru.solrudev.ackpine.DisposableSubscription
import ru.solrudev.ackpine.DisposableSubscriptionContainer
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.session.SessionResult
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.parameters.Confirmation
import ru.solrudev.ackpine.session.progress
import java.io.File

data object DownloadManager {

    private const val BUFFER_SIZE = 1024L

    private lateinit var httpClient: HttpClient
    private lateinit var file: File

    private val _progress = MutableStateFlow(Progress(0, 0))
    val progress: StateFlow<Progress> = _progress

    private val _downloadEnabled = MutableStateFlow(true)
    val downloadEnabled: StateFlow<Boolean> = _downloadEnabled

    fun setClient(httpClient: HttpClient) = apply {
        this.httpClient = httpClient
    }

    fun setFile(context: Context) = apply {
        setFile(context.filesDir.toOkioPath().resolve("v6").resolve("update.apk").toFile())
    }

    fun setFile(file: File) = apply {
        this.file = file
        this.file.deleteSafely()
    }

    suspend fun download(
        asset: Asset
    ): File? = withIOContext {
        if (!::httpClient.isInitialized) {
            return@withIOContext null
        }

        _downloadEnabled.update { false }
        file.deleteSafely()
        file.createAsFileSafely()

        httpClient.prepareGet(asset.downloadUrl) {
            onDownload { bytesSentTotal, contentLength ->
                _progress.update {
                    Progress(
                        reached = bytesSentTotal,
                        length = contentLength
                    )
                }
            }
        }.execute { response ->
            val channel = response.bodyAsChannel()

            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(BUFFER_SIZE)

                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()

                    file.appendBytes(bytes)
                }
            }
        }

        _downloadEnabled.update { true }
        return@withIOContext file
    }

    suspend fun install(context: Context, file: File = this.file): SessionResult<InstallFailure> {
        val packageInstaller = PackageInstaller.getInstance(context)
        return suspendCatching {
            packageInstaller.createSession(file.toUri()) {
                confirmation = Confirmation.IMMEDIATE
            }.await()
        }.getOrNull() ?: SessionResult.Error(InstallFailure.Generic())
    }

    @Serializable
    data class Progress(
        val reached: Long,
        val length: Long
    ) {
        val percentage: Float = if (length > reached) reached.toFloat() / length.toFloat() else 0F
    }
}