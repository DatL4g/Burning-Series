package dev.datlag.burningseries.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import dev.datlag.burningseries.LocalDarkMode
import dev.datlag.tooling.compose.platform.CombinedPlatformMaterialTheme

@Composable
fun DynamicMaterialTheme(
    seedColor: Color?,
    animate: Boolean = false,
    animationSpec: AnimationSpec<Color> = spring(stiffness = Spring.StiffnessLow),
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = if (seedColor != null) {
        rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = LocalDarkMode.current,
            style = PaletteStyle.TonalSpot,
            contrastLevel = Contrast.Default.value,
            isExtendedFidelity = false
        )
    } else {
        MaterialTheme.colorScheme
    }
    val animatedColorScheme = if (!animate) {
        dynamicColorScheme
    } else {
        dynamicColorScheme.copy(
            primary = dynamicColorScheme.primary.animate(animationSpec),
            primaryContainer = dynamicColorScheme.primaryContainer.animate(animationSpec),
            secondary = dynamicColorScheme.secondary.animate(animationSpec),
            secondaryContainer = dynamicColorScheme.secondaryContainer.animate(animationSpec),
            tertiary = dynamicColorScheme.tertiary.animate(animationSpec),
            tertiaryContainer = dynamicColorScheme.tertiaryContainer.animate(animationSpec),
            background = dynamicColorScheme.background.animate(animationSpec),
            surface = dynamicColorScheme.surface.animate(animationSpec),
            surfaceTint = dynamicColorScheme.surfaceTint.animate(animationSpec),
            surfaceBright = dynamicColorScheme.surfaceBright.animate(animationSpec),
            surfaceDim = dynamicColorScheme.surfaceDim.animate(animationSpec),
            surfaceContainer = dynamicColorScheme.surfaceContainer.animate(animationSpec),
            surfaceContainerHigh = dynamicColorScheme.surfaceContainerHigh.animate(animationSpec),
            surfaceContainerHighest = dynamicColorScheme.surfaceContainerHighest.animate(animationSpec),
            surfaceContainerLow = dynamicColorScheme.surfaceContainerLow.animate(animationSpec),
            surfaceContainerLowest = dynamicColorScheme.surfaceContainerLowest.animate(animationSpec),
            surfaceVariant = dynamicColorScheme.surfaceVariant.animate(animationSpec),
            error = dynamicColorScheme.error.animate(animationSpec),
            errorContainer = dynamicColorScheme.errorContainer.animate(animationSpec),
            onPrimary = dynamicColorScheme.onPrimary.animate(animationSpec),
            onPrimaryContainer = dynamicColorScheme.onPrimaryContainer.animate(animationSpec),
            onSecondary = dynamicColorScheme.onSecondary.animate(animationSpec),
            onSecondaryContainer = dynamicColorScheme.onSecondaryContainer.animate(animationSpec),
            onTertiary = dynamicColorScheme.onTertiary.animate(animationSpec),
            onTertiaryContainer = dynamicColorScheme.onTertiaryContainer.animate(animationSpec),
            onBackground = dynamicColorScheme.onBackground.animate(animationSpec),
            onSurface = dynamicColorScheme.onSurface.animate(animationSpec),
            onSurfaceVariant = dynamicColorScheme.onSurfaceVariant.animate(animationSpec),
            onError = dynamicColorScheme.onError.animate(animationSpec),
            onErrorContainer = dynamicColorScheme.onErrorContainer.animate(animationSpec),
            inversePrimary = dynamicColorScheme.inversePrimary.animate(animationSpec),
            inverseSurface = dynamicColorScheme.inverseSurface.animate(animationSpec),
            inverseOnSurface = dynamicColorScheme.inverseOnSurface.animate(animationSpec),
            outline = dynamicColorScheme.outline.animate(animationSpec),
            outlineVariant = dynamicColorScheme.outlineVariant.animate(animationSpec),
            scrim = dynamicColorScheme.scrim.animate(animationSpec),
        )
    }

    CombinedPlatformMaterialTheme(
        colorScheme = animatedColorScheme
    ) {
        content()
    }
}

@Composable
private fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
    return animateColorAsState(this, animationSpec).value
}