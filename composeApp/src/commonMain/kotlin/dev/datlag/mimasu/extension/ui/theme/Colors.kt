package dev.datlag.mimasu.extension.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data object Colors {

    const val THEME_LIGHT_PRIMARY = 0xff415f91
    private const val THEME_LIGHT_ON_PRIMARY = 0xffffffff
    private const val THEME_LIGHT_PRIMARY_CONTAINER = 0xffd6e3ff
    private const val THEME_LIGHT_ON_PRIMARY_CONTAINER = 0xff284777

    const val THEME_DARK_PRIMARY = 0xffaac7ff
    private const val THEME_DARK_ON_PRIMARY = 0xff0a305f
    private const val THEME_DARK_PRIMARY_CONTAINER = 0xff284777
    private const val THEME_DARK_ON_PRIMARY_CONTAINER = 0xffd6e3ff

    private const val THEME_LIGHT_SECONDARY = 0xff565f71
    private const val THEME_LIGHT_ON_SECONDARY = 0xffffffff
    private const val THEME_LIGHT_SECONDARY_CONTAINER = 0xffdae2f9
    private const val THEME_LIGHT_ON_SECONDARY_CONTAINER = 0xff3e4759

    private const val THEME_DARK_SECONDARY = 0xffbec6dc
    private const val THEME_DARK_ON_SECONDARY = 0xff283141
    private const val THEME_DARK_SECONDARY_CONTAINER = 0xff3e4759
    private const val THEME_DARK_ON_SECONDARY_CONTAINER = 0xffdae2f9

    private const val THEME_LIGHT_TERTIARY = 0xff276389
    private const val THEME_LIGHT_ON_TERTIARY = 0xffffffff
    private const val THEME_LIGHT_TERTIARY_CONTAINER = 0xffc9e6ff
    private const val THEME_LIGHT_ON_TERTIARY_CONTAINER = 0xff004b6f

    private const val THEME_DARK_TERTIARY = 0xff95cdf7
    private const val THEME_DARK_ON_TERTIARY = 0xff00344e
    private const val THEME_DARK_TERTIARY_CONTAINER = 0xff004b6f
    private const val THEME_DARK_ON_TERTIARY_CONTAINER = 0xffc9e6ff

    private const val THEME_LIGHT_ERROR = 0xffba1a1a
    private const val THEME_LIGHT_ON_ERROR = 0xffffffff
    private const val THEME_LIGHT_ERROR_CONTAINER = 0xffffdad6
    private const val THEME_LIGHT_ON_ERROR_CONTAINER = 0xff93000a

    private const val THEME_DARK_ERROR = 0xffffb4ab
    private const val THEME_DARK_ON_ERROR = 0xff690005
    private const val THEME_DARK_ERROR_CONTAINER = 0xff93000a
    private const val THEME_DARK_ON_ERROR_CONTAINER = 0xffffdad6

    private const val THEME_LIGHT_BACKGROUND = 0xffffffff
    private const val THEME_LIGHT_ON_BACKGROUND = 0xff000000

    private const val THEME_DARK_BACKGROUND = 0xff000000
    private const val THEME_DARK_ON_BACKGROUND = 0xffffffff

    private const val THEME_LIGHT_SURFACE = 0xfff9f9ff
    private const val THEME_LIGHT_ON_SURFACE = 0xff191c20
    private const val THEME_LIGHT_SURFACE_VARIANT = 0xfff9f9ff
    private const val THEME_LIGHT_ON_SURFACE_VARIANT = 0xff44474e

    private const val THEME_DARK_SURFACE = 0xff111318
    private const val THEME_DARK_ON_SURFACE = 0xffe2e2e9
    private const val THEME_DARK_SURFACE_VARIANT = 0xff37393e
    private const val THEME_DARK_ON_SURFACE_VARIANT = 0xffc4c6d0

    private const val THEME_LIGHT_OUTLINE = 0xff74777f
    private const val THEME_LIGHT_INVERSE_SURFACE = 0xff2e3036
    private const val THEME_LIGHT_INVERSE_ON_SURFACE = 0xfff0f0f7
    private const val THEME_LIGHT_INVERSE_PRIMARY = 0xffaac7ff

    private const val THEME_DARK_OUTLINE = 0xff8e9099
    private const val THEME_DARK_INVERSE_SURFACE = 0xffe2e2e9
    private const val THEME_DARK_INVERSE_ON_SURFACE = 0xff2e3036
    private const val THEME_DARK_INVERSE_PRIMARY = 0xff415f91

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

@Composable
expect fun Colors.dynamicDark(): ColorScheme

@Composable
expect fun Colors.dynamicLight(): ColorScheme