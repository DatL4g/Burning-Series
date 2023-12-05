package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Colors {

    private const val THEME_LIGHT_PRIMARY = 0xFF0c61a4
    private const val THEME_LIGHT_ON_PRIMARY = 0xFFffffff
    private const val THEME_LIGHT_PRIMARY_CONTAINER = 0xFFd2e4ff
    private const val THEME_LIGHT_ON_PRIMARY_CONTAINER = 0xFF001c37

    private const val THEME_DARK_PRIMARY = 0xFFa0c9ff
    private const val THEME_DARK_ON_PRIMARY = 0xFF00325a
    private const val THEME_DARK_PRIMARY_CONTAINER = 0xFF00497f
    private const val THEME_DARK_ON_PRIMARY_CONTAINER = 0xFFd2e4ff

    private const val THEME_LIGHT_SECONDARY = 0xFF535f70
    private const val THEME_LIGHT_ON_SECONDARY = 0xFFffffff
    private const val THEME_LIGHT_SECONDARY_CONTAINER = 0xFFd7e3f8
    private const val THEME_LIGHT_ON_SECONDARY_CONTAINER = 0xFF101c2b

    private const val THEME_DARK_SECONDARY = 0xFFbbc7db
    private const val THEME_DARK_ON_SECONDARY = 0xFF253141
    private const val THEME_DARK_SECONDARY_CONTAINER = 0xFF3c4858
    private const val THEME_DARK_ON_SECONDARY_CONTAINER = 0xFFd7e3f8

    private const val THEME_LIGHT_TERTIARY = 0xFF006686
    private const val THEME_LIGHT_ON_TERTIARY = 0xFFffffff
    private const val THEME_LIGHT_TERTIARY_CONTAINER = 0xFFc0e8ff
    private const val THEME_LIGHT_ON_TERTIARY_CONTAINER = 0xFF001e2b

    private const val THEME_DARK_TERTIARY = 0xFF70d2ff
    private const val THEME_DARK_ON_TERTIARY = 0xFF003547
    private const val THEME_DARK_TERTIARY_CONTAINER = 0xFF004d66
    private const val THEME_DARK_ON_TERTIARY_CONTAINER = 0xFFc0e8ff

    private const val THEME_LIGHT_ERROR = 0xFFba1a1a
    private const val THEME_LIGHT_ON_ERROR = 0xFFffffff
    private const val THEME_LIGHT_ERROR_CONTAINER = 0xFFffdad6
    private const val THEME_LIGHT_ON_ERROR_CONTAINER = 0xFF410002

    private const val THEME_DARK_ERROR = 0xFFffb4ab
    private const val THEME_DARK_ON_ERROR = 0xFF690005
    private const val THEME_DARK_ERROR_CONTAINER = 0xFF93000a
    private const val THEME_DARK_ON_ERROR_CONTAINER = 0xFFffdad6

    private const val THEME_LIGHT_BACKGROUND = 0xFF1a1c1e
    private const val THEME_LIGHT_ON_BACKGROUND = 0xFFe3e2e6

    private const val THEME_DARK_BACKGROUND = 0xFF191c1e
    private const val THEME_DARK_ON_BACKGROUND = 0xFFe2e2e5

    private const val THEME_LIGHT_SURFACE = 0xFFfdfcff
    private const val THEME_LIGHT_ON_SURFACE = 0xFF1a1c1e
    private const val THEME_LIGHT_SURFACE_VARIANT = 0xFFdfe2eb
    private const val THEME_LIGHT_ON_SURFACE_VARIANT = 0xFF43474e

    private const val THEME_DARK_SURFACE = 0xFF1a1c1e
    private const val THEME_DARK_ON_SURFACE = 0xFFe3e2e6
    private const val THEME_DARK_SURFACE_VARIANT = 0xFF43474e
    private const val THEME_DARK_ON_SURFACE_VARIANT = 0xFFc3c6cf

    private const val THEME_LIGHT_OUTLINE = 0xFF73777f
    private const val THEME_LIGHT_INVERSE_SURFACE = 0xFF2e3133
    private const val THEME_LIGHT_INVERSE_ON_SURFACE = 0xFFf0f0f3
    private const val THEME_LIGHT_INVERSE_PRIMARY = 0xFF88ceff

    private const val THEME_DARK_OUTLINE = 0xFF8d9199
    private const val THEME_DARK_INVERSE_SURFACE = 0xFFe2e2e5
    private const val THEME_DARK_INVERSE_ON_SURFACE = 0xFF2e3133
    private const val THEME_DARK_INVERSE_PRIMARY = 0xFF006590


    fun getDarkScheme() = darkColorScheme(
        primary = Color(THEME_DARK_PRIMARY),
        onPrimary = Color(THEME_DARK_ON_PRIMARY),
        primaryContainer = Color(THEME_DARK_PRIMARY_CONTAINER),
        onPrimaryContainer = Color(THEME_DARK_ON_PRIMARY_CONTAINER),

        secondary = Color(THEME_DARK_SECONDARY),
        onSecondary = Color(THEME_DARK_ON_SECONDARY),
        secondaryContainer = Color(THEME_DARK_SECONDARY_CONTAINER),
        onSecondaryContainer = Color(THEME_DARK_ON_SECONDARY_CONTAINER),

        tertiary = Color(THEME_DARK_TERTIARY),
        onTertiary = Color(THEME_DARK_ON_TERTIARY),
        tertiaryContainer = Color(THEME_DARK_TERTIARY_CONTAINER),
        onTertiaryContainer = Color(THEME_DARK_ON_TERTIARY_CONTAINER),

        error = Color(THEME_DARK_ERROR),
        errorContainer = Color(THEME_DARK_ERROR_CONTAINER),
        onError = Color(THEME_DARK_ON_ERROR),
        onErrorContainer = Color(THEME_DARK_ON_ERROR_CONTAINER),

        background = Color(THEME_DARK_BACKGROUND),
        onBackground = Color(THEME_DARK_ON_BACKGROUND),

        surface = Color(THEME_DARK_SURFACE),
        onSurface = Color(THEME_DARK_ON_SURFACE),
        surfaceVariant = Color(THEME_DARK_SURFACE_VARIANT),
        onSurfaceVariant = Color(THEME_DARK_ON_SURFACE_VARIANT),

        outline = Color(THEME_DARK_OUTLINE),
        inverseSurface = Color(THEME_DARK_INVERSE_SURFACE),
        inverseOnSurface = Color(THEME_DARK_INVERSE_ON_SURFACE),
        inversePrimary = Color(THEME_DARK_INVERSE_PRIMARY)
    )

    fun getLightScheme() = lightColorScheme(
        primary = Color(THEME_LIGHT_PRIMARY),
        onPrimary = Color(THEME_LIGHT_ON_PRIMARY),
        primaryContainer = Color(THEME_LIGHT_PRIMARY_CONTAINER),
        onPrimaryContainer = Color(THEME_LIGHT_ON_PRIMARY_CONTAINER),

        secondary = Color(THEME_LIGHT_SECONDARY),
        onSecondary = Color(THEME_LIGHT_ON_SECONDARY),
        secondaryContainer = Color(THEME_LIGHT_SECONDARY_CONTAINER),
        onSecondaryContainer = Color(THEME_LIGHT_ON_SECONDARY_CONTAINER),

        tertiary = Color(THEME_LIGHT_TERTIARY),
        onTertiary = Color(THEME_LIGHT_ON_TERTIARY),
        tertiaryContainer = Color(THEME_LIGHT_TERTIARY_CONTAINER),
        onTertiaryContainer = Color(THEME_LIGHT_ON_TERTIARY_CONTAINER),

        error = Color(THEME_LIGHT_ERROR),
        errorContainer = Color(THEME_LIGHT_ERROR_CONTAINER),
        onError = Color(THEME_LIGHT_ON_ERROR),
        onErrorContainer = Color(THEME_LIGHT_ON_ERROR_CONTAINER),

        background = Color(THEME_LIGHT_BACKGROUND),
        onBackground = Color(THEME_LIGHT_ON_BACKGROUND),

        surface = Color(THEME_LIGHT_SURFACE),
        onSurface = Color(THEME_LIGHT_ON_SURFACE),
        surfaceVariant = Color(THEME_LIGHT_SURFACE_VARIANT),
        onSurfaceVariant = Color(THEME_LIGHT_ON_SURFACE_VARIANT),

        outline = Color(THEME_LIGHT_OUTLINE),
        inverseSurface = Color(THEME_LIGHT_INVERSE_SURFACE),
        inverseOnSurface = Color(THEME_LIGHT_INVERSE_ON_SURFACE),
        inversePrimary = Color(THEME_LIGHT_INVERSE_PRIMARY)
    )

}