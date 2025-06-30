package dev.datlag.mimasu.extension

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.datlag.mimasu.extension.ui.navigation.Navigation
import dev.datlag.mimasu.extension.ui.theme.Colors
import dev.datlag.mimasu.extension.ui.theme.dynamicDark
import dev.datlag.mimasu.extension.ui.theme.dynamicLight
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformMaterialTheme
import dev.datlag.tooling.compose.platform.PlatformSurface
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.platform.rememberIsTv
import dev.datlag.tooling.compose.platform.typography
import org.kodein.di.DI
import org.kodein.di.compose.withDI

val LocalDarkMode = compositionLocalOf<Boolean> { error("No dark mode state provided") }

@Composable
fun App(
    di: DI,
    typography: Typography = Platform.typography(),
    systemDarkTheme: Boolean = isSystemInDarkTheme() || Platform.rememberIsTv(anyOS = true),
) = withDI(di) {
    CompositionLocalProvider(
        LocalDarkMode provides systemDarkTheme
    ) {
        PlatformMaterialTheme(
            colorScheme = if (systemDarkTheme) Colors.dynamicDark() else Colors.dynamicLight(),
            typography = typography
        ) {
            PlatformSurface(
                modifier = Modifier.fillMaxSize(),
                containerColor = Platform.colorScheme().background,
                contentColor = Platform.colorScheme().onBackground
            ) {
                Navigation()
            }
        }
    }
}