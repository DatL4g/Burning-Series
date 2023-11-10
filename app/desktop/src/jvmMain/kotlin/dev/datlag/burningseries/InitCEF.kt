package dev.datlag.burningseries

import androidx.compose.runtime.*
import dev.datlag.burningseries.common.LocalRestartRequired
import dev.datlag.burningseries.common.withIOContext
import dev.datlag.burningseries.other.CEFState
import dev.datlag.burningseries.other.LocalCEFInitialization
import dev.datlag.burningseries.window.ApplicationDisposer
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBuilder
import java.io.File

@Composable
fun InitCEF(content: @Composable () -> Unit) {
    val restartRequiredInitial = LocalRestartRequired.current
    var restartRequired by remember { mutableStateOf(restartRequiredInitial) }
    val cefState = remember { mutableStateOf<CEFState>(CEFState.LOCATING) }

    LaunchedEffect(ApplicationDisposer.current) {
        withIOContext {
            KCEF.init(
                builder = {
                    installDir(File(AppIO.getWriteableExecutableFolder(), "kcef-bundle"))
                    progress {
                        onLocating {
                            cefState.value = CEFState.LOCATING
                        }
                        onDownloading {
                            cefState.value = CEFState.Downloading(it)
                        }
                        onExtracting {
                            cefState.value = CEFState.EXTRACTING
                        }
                        onInstall {
                            cefState.value = CEFState.INSTALLING
                        }
                        onInitializing {
                            cefState.value = CEFState.INITIALIZING
                        }
                        onInitialized {
                            cefState.value = CEFState.INITIALIZED
                        }
                    }
                    settings {
                        logSeverity = KCEFBuilder.Settings.LogSeverity.Disable
                    }
                },
                onRestartRequired = {
                    restartRequired = true
                }
            )
        }
    }

    CompositionLocalProvider(
        LocalRestartRequired provides restartRequired,
        LocalCEFInitialization provides cefState
    ) {
        content()
    }
}