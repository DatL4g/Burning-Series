package dev.datlag.burningseries.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.datlag.burningseries.LocalResources

fun androidx.compose.material3.ColorScheme.toLegacyColors(isDark: Boolean): Colors {
    return if (isDark) {
        darkColors(
            primary = this.primary,
            primaryVariant = this.primaryContainer,
            secondary = this.secondary,
            secondaryVariant = this.secondaryContainer,
            background = this.background,
            surface = this.surface,
            error = this.error,
            onPrimary = this.onPrimary,
            onSecondary = this.onSecondary,
            onBackground = this.onBackground,
            onSurface = this.onSurface,
            onError = this.onError
        )
    } else {
        lightColors(
            primary = this.primary,
            primaryVariant = this.primaryContainer,
            secondary = this.secondary,
            secondaryVariant = this.secondaryContainer,
            background = this.background,
            surface = this.surface,
            error = this.error,
            onPrimary = this.onPrimary,
            onSecondary = this.onSecondary,
            onBackground = this.onBackground,
            onSurface = this.onSurface,
            onError = this.onError
        )
    }
}

fun androidx.compose.material3.Shapes.toLegacyShapes(): Shapes {
    return Shapes(this.small, this.medium, this.large)
}

@Composable
fun ManropeTypography(): Typography {
    val fontFamily = LocalResources.current.Manrope

    return remember {
        Typography(
            displayLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                lineHeight = 64.0.sp,
                letterSpacing = (-0.2).sp,
            ),
            displayMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                lineHeight = 52.0.sp,
                letterSpacing = 0.0.sp
            ),
            displaySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 44.0.sp,
                letterSpacing = 0.0.sp
            ),
            headlineLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.0.sp,
                letterSpacing = 0.0.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.0.sp,
                letterSpacing = 0.0.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.0.sp,
                letterSpacing = 0.0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.0.sp,
                letterSpacing = 0.0.sp
            ),
            titleMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.0.sp,
                letterSpacing = 0.2.sp
            ),
            titleSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.0.sp,
                letterSpacing = 0.1.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.0.sp,
                letterSpacing = 0.5.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.0.sp,
                letterSpacing = 0.2.sp
            ),
            bodySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.0.sp,
                letterSpacing = 0.4.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.0.sp,
                letterSpacing = 0.1.sp
            ),
            labelMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.0.sp,
                letterSpacing = 0.5.sp
            ),
            labelSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.0.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
fun ManropeTypographyLegacy(): androidx.compose.material.Typography {
    val fontFamily = LocalResources.current.Manrope

    return remember { androidx.compose.material.Typography(
        defaultFontFamily = fontFamily
    ) }
}