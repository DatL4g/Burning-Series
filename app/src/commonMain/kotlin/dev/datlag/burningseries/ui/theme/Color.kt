package dev.datlag.burningseries.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Colors {
    private const val THEME_PRIMARY = 0xFF0c61a4
    private const val THEME_ON_PRIMARY = 0xFFfafafa
    private const val THEME_PRIMARY_CONTAINER = 0xFFd2e4ff
    private const val THEME_ON_PRIMARY_CONTAINER = 0xFF001c37

    private const val THEME_SECONDARY = 0xFF006780
    private const val THEME_ON_SECONDARY = 0xFFfafafa
    private const val THEME_SECONDARY_CONTAINER = 0xFFB8EAFF
    private const val THEME_ON_SECONDARY_CONTAINER = 0xFF001F28

    private const val THEME_LIGHT_TERTIARY = 0xFF2B5EA7
    private const val THEME_DARK_TERTIARY = 0xFF2c2c2c
    private const val THEME_LIGHT_ON_TERTIARY = 0xFFfafafa
    private const val THEME_DARK_ON_TERTIARY = 0xFFfafafa
    private const val THEME_LIGHT_TERTIARY_CONTAINER = 0xFFD7E3FF
    private const val THEME_DARK_TERTIARY_CONTAINER = 0xFF555555
    private const val THEME_LIGHT_ON_TERTIARY_CONTAINER = 0xFF001B3E
    private const val THEME_DARK_ON_TERTIARY_CONTAINER = 0xFFfafafa

    private const val THEME_ERROR = 0xFFdb3236
    private const val THEME_ON_ERROR = 0xFFfafafa
    private const val THEME_ERROR_CONTAINER = 0xFFffdad7
    private const val THEME_ON_ERROR_CONTAINER = 0xFF410004

    private const val THEME_DARK_BACKGROUND = 0xFF1A1C1E
    private const val THEME_DARK_ON_BACKGROUND = 0xFFE3E2E6
    private const val THEME_LIGHT_BACKGROUND = 0xFFFDFCFF
    private const val THEME_LIGHT_ON_BACKGROUND = 0xFF1A1C1E

    private const val THEME_DARK_SURFACE = 0xFF1A1C1E
    private const val THEME_LIGHT_SURFACE = 0xFFFDFCFF
    private const val THEME_DARK_ON_SURFACE = 0xFFE3E2E6
    private const val THEME_LIGHT_ON_SURFACE = 0xFF1A1C1E
    private const val THEME_DARK_SURFACE_VARIANT = 0xFF43474E
    private const val THEME_LIGHT_SURFACE_VARIANT = 0xFFDFE2EB
    private const val THEME_DARK_ON_SURFACE_VARIANT = 0xFFC3C6CF
    private const val THEME_LIGHT_ON_SURFACE_VARIANT = 0xFF43474E

    private const val THEME_DARK_OUTLINE = 0xFF8D9199
    private const val THEME_LIGHT_OUTLINE = 0xFF73777F
    private const val THEME_DARK_INVERSE_SURFACE = 0xFFE3E2E6
    private const val THEME_LIGHT_INVERSE_SURFACE = 0xFF2F3033
    private const val THEME_DARK_INVERSE_ON_SURFACE = 0xFF1A1C1E
    private const val THEME_LIGHT_INVERSE_ON_SURFACE = 0xFFF1F0F4
    private const val THEME_INVERSE_PRIMARY = 0xFF0B61A4

    fun getDarkScheme() = darkColorScheme(
        primary = Color(THEME_PRIMARY),
        onPrimary = Color(THEME_ON_PRIMARY),
        primaryContainer = Color(THEME_PRIMARY_CONTAINER),
        onPrimaryContainer = Color(THEME_ON_PRIMARY_CONTAINER),

        secondary = Color(THEME_SECONDARY),
        onSecondary = Color(THEME_ON_SECONDARY),
        secondaryContainer = Color(THEME_SECONDARY_CONTAINER),
        onSecondaryContainer = Color(THEME_ON_SECONDARY_CONTAINER),

        tertiary = Color(THEME_DARK_TERTIARY),
        onTertiary = Color(THEME_DARK_ON_TERTIARY),
        tertiaryContainer = Color(THEME_DARK_TERTIARY_CONTAINER),
        onTertiaryContainer = Color(THEME_DARK_ON_TERTIARY_CONTAINER),

        error = Color(THEME_ERROR),
        errorContainer = Color(THEME_ERROR_CONTAINER),
        onError = Color(THEME_ON_ERROR),
        onErrorContainer = Color(THEME_ON_ERROR_CONTAINER),

        background = Color(THEME_DARK_BACKGROUND),
        onBackground = Color(THEME_DARK_ON_BACKGROUND),

        surface = Color(THEME_DARK_SURFACE),
        onSurface = Color(THEME_DARK_ON_SURFACE),
        surfaceVariant = Color(THEME_DARK_SURFACE_VARIANT),
        onSurfaceVariant = Color(THEME_DARK_ON_SURFACE_VARIANT),

        outline = Color(THEME_DARK_OUTLINE),
        inverseOnSurface = Color(THEME_DARK_INVERSE_ON_SURFACE),
        inverseSurface = Color(THEME_DARK_INVERSE_SURFACE),
        inversePrimary = Color(THEME_INVERSE_PRIMARY)
    )

    fun getLightScheme() = lightColorScheme(
        primary = Color(THEME_PRIMARY),
        onPrimary = Color(THEME_ON_PRIMARY),
        primaryContainer = Color(THEME_PRIMARY_CONTAINER),
        onPrimaryContainer = Color(THEME_ON_PRIMARY_CONTAINER),

        secondary = Color(THEME_SECONDARY),
        onSecondary = Color(THEME_ON_SECONDARY),
        secondaryContainer = Color(THEME_SECONDARY_CONTAINER),
        onSecondaryContainer = Color(THEME_ON_SECONDARY_CONTAINER),

        tertiary = Color(THEME_LIGHT_TERTIARY),
        onTertiary = Color(THEME_LIGHT_ON_TERTIARY),
        tertiaryContainer = Color(THEME_LIGHT_TERTIARY_CONTAINER),
        onTertiaryContainer = Color(THEME_LIGHT_ON_TERTIARY_CONTAINER),

        error = Color(THEME_ERROR),
        errorContainer = Color(THEME_ERROR_CONTAINER),
        onError = Color(THEME_ON_ERROR),
        onErrorContainer = Color(THEME_ON_ERROR_CONTAINER),

        background = Color(THEME_LIGHT_BACKGROUND),
        onBackground = Color(THEME_LIGHT_ON_BACKGROUND),

        surface = Color(THEME_LIGHT_SURFACE),
        onSurface = Color(THEME_LIGHT_ON_SURFACE),
        surfaceVariant = Color(THEME_LIGHT_SURFACE_VARIANT),
        onSurfaceVariant = Color(THEME_LIGHT_ON_SURFACE_VARIANT),

        outline = Color(THEME_LIGHT_OUTLINE),
        inverseOnSurface = Color(THEME_LIGHT_INVERSE_ON_SURFACE),
        inverseSurface = Color(THEME_LIGHT_INVERSE_SURFACE),
        inversePrimary = Color(THEME_INVERSE_PRIMARY)
    )
}