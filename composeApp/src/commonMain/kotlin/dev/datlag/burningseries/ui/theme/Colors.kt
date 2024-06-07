package dev.datlag.burningseries.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data object Colors {

    private const val THEME_LIGHT_PRIMARY = 0xFF894a68
    private const val THEME_LIGHT_ON_PRIMARY = 0xFFffffff
    private const val THEME_LIGHT_PRIMARY_CONTAINER = 0xFFffd8e7
    private const val THEME_LIGHT_ON_PRIMARY_CONTAINER = 0xFF380723

    private const val THEME_DARK_PRIMARY = 0xFFfeb0d2
    private const val THEME_DARK_ON_PRIMARY = 0xFF521d39
    private const val THEME_DARK_PRIMARY_CONTAINER = 0xFF6d3350
    private const val THEME_DARK_ON_PRIMARY_CONTAINER = 0xFFffd8e7

    private const val THEME_LIGHT_SECONDARY = 0xFF725762
    private const val THEME_LIGHT_ON_SECONDARY = 0xFFffffff
    private const val THEME_LIGHT_SECONDARY_CONTAINER = 0xFFfed9e6
    private const val THEME_LIGHT_ON_SECONDARY_CONTAINER = 0xFF2a151f

    private const val THEME_DARK_SECONDARY = 0xFFe0bdca
    private const val THEME_DARK_ON_SECONDARY = 0xFF412a34
    private const val THEME_DARK_SECONDARY_CONTAINER = 0xFF59404a
    private const val THEME_DARK_ON_SECONDARY_CONTAINER = 0xFFfed9e6

    private const val THEME_LIGHT_TERTIARY = 0xFF864B6E
    private const val THEME_LIGHT_ON_TERTIARY = 0xFFFFFFFF
    private const val THEME_LIGHT_TERTIARY_CONTAINER = 0xFFFFD8EA
    private const val THEME_LIGHT_ON_TERTIARY_CONTAINER = 0xFF370728

    private const val THEME_DARK_TERTIARY = 0xFFFBB1D8
    private const val THEME_DARK_ON_TERTIARY = 0xFF511D3E
    private const val THEME_DARK_TERTIARY_CONTAINER = 0xFF6B3455
    private const val THEME_DARK_ON_TERTIARY_CONTAINER = 0xFFFFD8EA

    private const val THEME_LIGHT_ERROR = 0xFFba1a1a
    private const val THEME_LIGHT_ON_ERROR = 0xFFffffff
    private const val THEME_LIGHT_ERROR_CONTAINER = 0xFFffdad6
    private const val THEME_LIGHT_ON_ERROR_CONTAINER = 0xFF410002

    private const val THEME_DARK_ERROR = 0xFFffb4ab
    private const val THEME_DARK_ON_ERROR = 0xFF690005
    private const val THEME_DARK_ERROR_CONTAINER = 0xFF93000a
    private const val THEME_DARK_ON_ERROR_CONTAINER = 0xFFffdad6

    private const val THEME_LIGHT_BACKGROUND = 0xFFFCF8FF
    private const val THEME_LIGHT_ON_BACKGROUND = 0xFF1B1B21

    private const val THEME_DARK_BACKGROUND = 0xFF131318
    private const val THEME_DARK_ON_BACKGROUND = 0xFFE4E1E9

    private const val THEME_LIGHT_SURFACE = 0xFFfff0f4
    private const val THEME_LIGHT_ON_SURFACE = 0xFF21191c
    private const val THEME_LIGHT_SURFACE_VARIANT = 0xFFfaeaee
    private const val THEME_LIGHT_ON_SURFACE_VARIANT = 0xFF504348

    private const val THEME_DARK_SURFACE = 0xFF21191c
    private const val THEME_DARK_ON_SURFACE = 0xFFeedfe3
    private const val THEME_DARK_SURFACE_VARIANT = 0xFF251d20
    private const val THEME_DARK_ON_SURFACE_VARIANT = 0xFFd4c2c7

    private const val THEME_LIGHT_OUTLINE = 0xFF827378
    private const val THEME_LIGHT_INVERSE_SURFACE = 0xFF372e31
    private const val THEME_LIGHT_INVERSE_ON_SURFACE = 0xFFfdedf1
    private const val THEME_LIGHT_INVERSE_PRIMARY = 0xFFfeb0d2

    private const val THEME_DARK_OUTLINE = 0xFF9d8d92
    private const val THEME_DARK_INVERSE_SURFACE = 0xFFeedfe3
    private const val THEME_DARK_INVERSE_ON_SURFACE = 0xFF372e31
    private const val THEME_DARK_INVERSE_PRIMARY = 0xFF894a68

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