package dev.datlag.burningseries.color.dynamiccolor

import dev.datlag.burningseries.color.common.Function
import dev.datlag.burningseries.color.dislike.DislikeAnalyzer.fixIfDisliked
import dev.datlag.burningseries.color.hct.Hct
import dev.datlag.burningseries.color.hct.ViewingConditions
import dev.datlag.burningseries.color.scheme.DynamicScheme
import dev.datlag.burningseries.color.scheme.Variant
import kotlin.math.abs
import kotlin.math.max


object MaterialDynamicColors {

    private const val CONTAINER_ACCENT_TONE_DELTA = 15.0

    fun highestSurface(s: DynamicScheme): DynamicColor {
        return if (s.isDark) surfaceBright() else surfaceDim()
    }

    // Compatibility Keys Colors for Android
    fun primaryPaletteKeyColor(): DynamicColor {
        return DynamicColor.fromPalette(
            name = "primary_palette_key_color",
            palette = {
                it.primaryPalette
            },
            tone = {
                it.primaryPalette.keyColor.tone
            }
        )
    }

    fun secondaryPaletteKeyColor(): DynamicColor {
        return DynamicColor.fromPalette(
            name = "secondary_palette_key_color",
            palette = {
                it.secondaryPalette
            },
            tone = {
                it.secondaryPalette.keyColor.tone
            }
        )
    }

    fun tertiaryPaletteKeyColor(): DynamicColor {
        return DynamicColor.fromPalette(
            name = "tertiary_palette_key_color",
            palette = {
                it.tertiaryPalette
            },
            tone = {
                it.tertiaryPalette.keyColor.tone
            }
        )
    }

    fun neutralVariantPaletteKeyColor(): DynamicColor {
        return DynamicColor.fromPalette( /* name= */
            "neutral_variant_palette_key_color",  /* palette= */
            { s -> s.neutralVariantPalette }
        )  /* tone= */
        { s -> s.neutralVariantPalette.keyColor.tone }
    }

    fun background(): DynamicColor {
        return DynamicColor( /* name= */
            "background",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 6.0 else 98.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun onBackground(): DynamicColor {
        return DynamicColor( /* name= */
            "on_background",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 90.0 else 10.0 },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> background() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(3.0, 3.0, 4.5, 7.0),  /* toneDeltaPair= */
            null
        )
    }

    fun surface(): DynamicColor {
        return DynamicColor( /* name= */
            "surface",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 6.0 else 98.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceDim(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_dim",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 6.0 else 87.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceBright(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_bright",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 24.0 else 98.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceContainerLowest(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_container_lowest",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 4.0 else 100.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceContainerLow(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_container_low",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 10.0 else 96.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_container",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 12.0 else 94.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceContainerHigh(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_container_high",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 17.0 else 92.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceContainerHighest(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_container_highest",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 22.0 else 90.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun onSurface(): DynamicColor {
        return DynamicColor( /* name= */
            "on_surface",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 90.0 else 10.0 },  /* isBackground= */
            false,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceVariant(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_variant",  /* palette= */
            { s: DynamicScheme -> s.neutralVariantPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 30.0 else 90.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun onSurfaceVariant(): DynamicColor {
        return DynamicColor( /* name= */
            "on_surface_variant",  /* palette= */
            { s: DynamicScheme -> s.neutralVariantPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 80.0 else 30.0 },  /* isBackground= */
            false,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0),  /* toneDeltaPair= */
            null
        )
    }

    fun inverseSurface(): DynamicColor {
        return DynamicColor( /* name= */
            "inverse_surface",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 90.0 else 20.0 },  /* isBackground= */
            false,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun inverseOnSurface(): DynamicColor {
        return DynamicColor( /* name= */
            "inverse_on_surface",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 20.0 else 95.0 },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> inverseSurface() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun outline(): DynamicColor {
        return DynamicColor( /* name= */
            "outline",  /* palette= */
            { s: DynamicScheme -> s.neutralVariantPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 60.0 else 50.0 },  /* isBackground= */
            false,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.5, 3.0, 4.5, 7.0),  /* toneDeltaPair= */
            null
        )
    }

    fun outlineVariant(): DynamicColor {
        return DynamicColor( /* name= */
            "outline_variant",  /* palette= */
            { s: DynamicScheme -> s.neutralVariantPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 30.0 else 80.0 },  /* isBackground= */
            false,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0),  /* toneDeltaPair= */
            null
        )
    }

    fun shadow(): DynamicColor {
        return DynamicColor( /* name= */
            "shadow",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme? -> 0.0 },  /* isBackground= */
            false,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun scrim(): DynamicColor {
        return DynamicColor( /* name= */
            "scrim",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme? -> 0.0 },  /* isBackground= */
            false,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun surfaceTint(): DynamicColor {
        return DynamicColor( /* name= */
            "surface_tint",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 80.0 else 40.0 },  /* isBackground= */
            true,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )
    }

    fun primary(): DynamicColor {
        return DynamicColor( /* name= */
            "primary",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 100.0 else 0.0
                }
                if (s.isDark) 80.0 else 40.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                primaryContainer(),
                primary(), 15.0, TonePolarity.NEARER, false
            )
        }
    }

    fun onPrimary(): DynamicColor {
        return DynamicColor( /* name= */
            "on_primary",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 10.0 else 90.0
                }
                if (s.isDark) 20.0 else 100.0
            },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> primary() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun primaryContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "primary_container",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isFidelity(s)) {
                    return@Function performAlbers(s.sourceColorHct, s)
                }
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 85.0 else 25.0
                }
                if (s.isDark) 30.0 else 90.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                primaryContainer(),
                primary(), 15.0, TonePolarity.NEARER, false
            )
        }
    }

    fun onPrimaryContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "on_primary_container",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isFidelity(s)) {
                    return@Function DynamicColor.foregroundTone(
                        primaryContainer().tone.apply(s),
                        4.5
                    )
                }
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 0.0 else 100.0
                }
                if (s.isDark) 90.0 else 10.0
            },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> primaryContainer() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun inversePrimary(): DynamicColor {
        return DynamicColor( /* name= */
            "inverse_primary",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 40.0 else 80.0 },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> inverseSurface() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0),  /* toneDeltaPair= */
            null
        )
    }

    fun secondary(): DynamicColor {
        return DynamicColor( /* name= */
            "secondary",  /* palette= */
            { s: DynamicScheme -> s.secondaryPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 80.0 else 40.0 },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                secondaryContainer(),
                secondary(), 15.0, TonePolarity.NEARER, false
            )
        }
    }

    fun onSecondary(): DynamicColor {
        return DynamicColor( /* name= */
            "on_secondary",  /* palette= */
            { s: DynamicScheme -> s.secondaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 10.0 else 100.0
                } else {
                    return@Function if (s.isDark) 20.0 else 100.0
                }
            },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> secondary() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun secondaryContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "secondary_container",  /* palette= */
            { s: DynamicScheme -> s.secondaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                val initialTone = if (s.isDark) 30.0 else 90.0
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 30.0 else 85.0
                }
                if (!isFidelity(s)) {
                    return@Function initialTone
                }
                var answer = findDesiredChromaByTone(
                    s.secondaryPalette.hue,
                    s.secondaryPalette.chroma,
                    initialTone,
                    !s.isDark
                )
                answer = performAlbers(s.secondaryPalette.getHct(answer), s)
                answer
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                secondaryContainer(),
                secondary(), 15.0, TonePolarity.NEARER, false
            )
        }
    }

    fun onSecondaryContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "on_secondary_container",  /* palette= */
            { s: DynamicScheme -> s.secondaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (!isFidelity(s)) {
                    return@Function if (s.isDark) 90.0 else 10.0
                }
                DynamicColor.foregroundTone(
                    secondaryContainer().tone.apply(s),
                    4.5
                )
            },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> secondaryContainer() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun tertiary(): DynamicColor {
        return DynamicColor( /* name= */
            "tertiary",  /* palette= */
            { s: DynamicScheme -> s.tertiaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 90.0 else 25.0
                }
                if (s.isDark) 80.0 else 40.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                tertiaryContainer(),
                tertiary(), 15.0, TonePolarity.NEARER, false
            )
        }
    }

    fun onTertiary(): DynamicColor {
        return DynamicColor( /* name= */
            "on_tertiary",  /* palette= */
            { s: DynamicScheme -> s.tertiaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 10.0 else 90.0
                }
                if (s.isDark) 20.0 else 100.0
            },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> tertiary() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun tertiaryContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "tertiary_container",  /* palette= */
            { s: DynamicScheme -> s.tertiaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 60.0 else 49.0
                }
                if (!isFidelity(s)) {
                    return@Function if (s.isDark) 30.0 else 90.0
                }
                val albersTone = performAlbers(s.tertiaryPalette.getHct(s.sourceColorHct.tone), s)
                val proposedHct = s.tertiaryPalette.getHct(albersTone)
                fixIfDisliked(proposedHct).tone
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                tertiaryContainer(),
                tertiary(), 15.0, TonePolarity.NEARER, false
            )
        }
    }

    fun onTertiaryContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "on_tertiary_container",  /* palette= */
            { s: DynamicScheme -> s.tertiaryPalette },  /* tone= */
            Function { s: DynamicScheme ->
                if (isMonochrome(s)) {
                    return@Function if (s.isDark) 0.0 else 100.0
                }
                if (!isFidelity(s)) {
                    return@Function if (s.isDark) 90.0 else 10.0
                }
                DynamicColor.foregroundTone(
                    tertiaryContainer().tone.apply(s),
                    4.5
                )
            },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> tertiaryContainer() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun error(): DynamicColor {
        return DynamicColor( /* name= */
            "error",  /* palette= */
            { s: DynamicScheme -> s.errorPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 80.0 else 40.0 },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                errorContainer(),
                error(), 15.0, TonePolarity.NEARER, false
            )
        }
    }

    fun onError(): DynamicColor {
        return DynamicColor( /* name= */
            "on_error",  /* palette= */
            { s: DynamicScheme -> s.errorPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 20.0 else 100.0 },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> error() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun errorContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "error_container",  /* palette= */
            { s: DynamicScheme -> s.errorPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 30.0 else 90.0 },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                errorContainer(),
                error(), 15.0, TonePolarity.NEARER, false
            )
        }
    }

    fun onErrorContainer(): DynamicColor {
        return DynamicColor( /* name= */
            "on_error_container",  /* palette= */
            { s: DynamicScheme -> s.errorPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 90.0 else 10.0 },  /* isBackground= */
            false,  /* background= */
            { s: DynamicScheme? -> errorContainer() },  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun primaryFixed(): DynamicColor {
        return DynamicColor( /* name= */
            "primary_fixed",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 40.0 else 90.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                primaryFixed(),
                primaryFixedDim(), 10.0, TonePolarity.LIGHTER, true
            )
        }
    }

    fun primaryFixedDim(): DynamicColor {
        return DynamicColor( /* name= */
            "primary_fixed_dim",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 30.0 else 80.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                primaryFixed(),
                primaryFixedDim(), 10.0, TonePolarity.LIGHTER, true
            )
        }
    }

    fun onPrimaryFixed(): DynamicColor {
        return DynamicColor( /* name= */
            "on_primary_fixed",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 100.0 else 10.0
            },  /* isBackground= */
            false,  /* background= */
            { s -> primaryFixedDim() },  /* secondBackground= */
            { s -> primaryFixed() },  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun onPrimaryFixedVariant(): DynamicColor {
        return DynamicColor( /* name= */
            "on_primary_fixed_variant",  /* palette= */
            { s: DynamicScheme -> s.primaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(s)) 90.0 else 30.0
            },  /* isBackground= */
            false,  /* background= */
            { s -> primaryFixedDim() },  /* secondBackground= */
            { s -> primaryFixed() },  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0),  /* toneDeltaPair= */
            null
        )
    }

    fun secondaryFixed(): DynamicColor {
        return DynamicColor( /* name= */
            "secondary_fixed",  /* palette= */
            { s: DynamicScheme -> s.secondaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 80.0 else 90.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                secondaryFixed(),
                secondaryFixedDim(), 10.0, TonePolarity.LIGHTER, true
            )
        }
    }

    fun secondaryFixedDim(): DynamicColor {
        return DynamicColor( /* name= */
            "secondary_fixed_dim",  /* palette= */
            { s: DynamicScheme -> s.secondaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 70.0 else 80.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s: DynamicScheme? ->
            ToneDeltaPair(
                secondaryFixed(),
                secondaryFixedDim(), 10.0, TonePolarity.LIGHTER, true
            )
        }
    }

    fun onSecondaryFixed(): DynamicColor {
        return DynamicColor( /* name= */
            "on_secondary_fixed",  /* palette= */
            { s: DynamicScheme -> s.secondaryPalette },  /* tone= */
            { s -> 10.0 },  /* isBackground= */
            false,  /* background= */
            { s -> secondaryFixedDim() },  /* secondBackground= */
            { s -> secondaryFixed() },  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun onSecondaryFixedVariant(): DynamicColor {
        return DynamicColor( /* name= */
            "on_secondary_fixed_variant",  /* palette= */
            { s: DynamicScheme -> s.secondaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 25.0 else 30.0
            },  /* isBackground= */
            false,  /* background= */
            { s -> secondaryFixedDim() },  /* secondBackground= */
            { s -> secondaryFixed() },  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0),  /* toneDeltaPair= */
            null
        )
    }

    fun tertiaryFixed(): DynamicColor {
        return DynamicColor( /* name= */
            "tertiary_fixed",  /* palette= */
            { s: DynamicScheme -> s.tertiaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 40.0 else 90.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s ->
            ToneDeltaPair(
                tertiaryFixed(),
                tertiaryFixedDim(), 10.0, TonePolarity.LIGHTER, true
            )
        }
    }

    fun tertiaryFixedDim(): DynamicColor {
        return DynamicColor( /* name= */
            "tertiary_fixed_dim",  /* palette= */
            { s: DynamicScheme -> s.tertiaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 30.0 else 80.0
            },  /* isBackground= */
            true,  /* background= */
            MaterialDynamicColors::highestSurface,  /* secondBackground= */
            null,  /* contrastCurve= */
            ContrastCurve(1.0, 1.0, 3.0, 7.0)
        )  /* toneDeltaPair= */
        { s ->
            ToneDeltaPair(
                tertiaryFixed(),
                tertiaryFixedDim(), 10.0, TonePolarity.LIGHTER, true
            )
        }
    }

    fun onTertiaryFixed(): DynamicColor {
        return DynamicColor( /* name= */
            "on_tertiary_fixed",  /* palette= */
            { s: DynamicScheme -> s.tertiaryPalette },  /* tone= */
            { s ->
                if (isMonochrome(
                        s
                    )
                ) 100.0 else 10.0
            },  /* isBackground= */
            false,  /* background= */
            { s -> tertiaryFixedDim() },  /* secondBackground= */
            { s -> tertiaryFixed() },  /* contrastCurve= */
            ContrastCurve(4.5, 7.0, 11.0, 21.0),  /* toneDeltaPair= */
            null
        )
    }

    fun onTertiaryFixedVariant(): DynamicColor {
        return DynamicColor( /* name= */
            "on_tertiary_fixed_variant",  /* palette= */
            { s: DynamicScheme -> s.tertiaryPalette },  /* tone= */
            { s -> if (isMonochrome(s)) 90.0 else 30.0 },  /* isBackground= */
            false,  /* background= */
            { s -> tertiaryFixedDim() },  /* secondBackground= */
            { s -> tertiaryFixed() },  /* contrastCurve= */
            ContrastCurve(3.0, 4.5, 7.0, 11.0),  /* toneDeltaPair= */
            null
        )
    }

    /**
     * These colors were present in Android framework before Android U, and used by MDC controls. They
     * should be avoided, if possible. It's unclear if they're used on multiple backgrounds, and if
     * they are, they can't be adjusted for contrast.* For now, they will be set with no background,
     * and those won't adjust for contrast, avoiding issues.
     *
     *
     * * For example, if the same color is on a white background _and_ black background, there's no
     * way to increase contrast with either without losing contrast with the other.
     */
    // colorControlActivated documented as colorAccent in M3 & GM3.
    // colorAccent documented as colorSecondary in M3 and colorPrimary in GM3.
    // Android used Material's Container as Primary/Secondary/Tertiary at launch.
    // Therefore, this is a duplicated version of Primary Container.
    fun controlActivated(): DynamicColor {
        return DynamicColor.fromPalette(
            "control_activated", { s -> s.primaryPalette }) { s -> if (s.isDark) 30.0 else 90.0 }
    }

    // colorControlNormal documented as textColorSecondary in M3 & GM3.
    // In Material, textColorSecondary points to onSurfaceVariant in the non-disabled state,
    // which is Neutral Variant T30/80 in light/dark.
    fun controlNormal(): DynamicColor {
        return DynamicColor.fromPalette(
            "control_normal", { s -> s.neutralVariantPalette }) { s -> if (s.isDark) 80.0 else 30.0 }
    }

    // colorControlHighlight documented, in both M3 & GM3:
    // Light mode: #1f000000 dark mode: #33ffffff.
    // These are black and white with some alpha.
    // 1F hex = 31 decimal; 31 / 255 = 12% alpha.
    // 33 hex = 51 decimal; 51 / 255 = 20% alpha.
    // DynamicColors do not support alpha currently, and _may_ not need it for this use case,
    // depending on how MDC resolved alpha for the other cases.
    // Returning black in dark mode, white in light mode.
    fun controlHighlight(): DynamicColor {
        return DynamicColor( /* name= */
            "control_highlight",  /* palette= */
            { s: DynamicScheme -> s.neutralPalette },  /* tone= */
            { s: DynamicScheme -> if (s.isDark) 100.0 else 0.0 },  /* isBackground= */
            false,  /* background= */
            null,  /* secondBackground= */
            null,  /* contrastCurve= */
            null,  /* toneDeltaPair= */
            null
        )  /* opacity= */
        { s: DynamicScheme -> if (s.isDark) 0.20 else 0.12 }
    }

    // textColorPrimaryInverse documented, in both M3 & GM3, documented as N10/N90.
    fun textPrimaryInverse(): DynamicColor {
        return DynamicColor.fromPalette(
            "text_primary_inverse", { s -> s.neutralPalette }) { s -> if (s.isDark) 10.0 else 90.0 }
    }

    // textColorSecondaryInverse and textColorTertiaryInverse both documented, in both M3 & GM3, as
    fun textSecondaryAndTertiaryInverse(): DynamicColor {
        return DynamicColor.fromPalette(
            "text_secondary_and_tertiary_inverse",
            { s -> s.neutralVariantPalette }
        ) { s -> if (s.isDark) 30.0 else 80.0 }
    }

    // textColorPrimaryInverseDisableOnly documented, in both M3 & GM3, as N10/N90
    fun textPrimaryInverseDisableOnly(): DynamicColor {
        return DynamicColor.fromPalette(
            "text_primary_inverse_disable_only",
            { s -> s.neutralPalette }
        ) { s -> if (s.isDark) 10.0 else 90.0 }
    }

    // textColorSecondaryInverse and textColorTertiaryInverse in disabled state both documented,
    // in both M3 & GM3, as N10/N90
    fun textSecondaryAndTertiaryInverseDisabled(): DynamicColor {
        return DynamicColor.fromPalette(
            "text_secondary_and_tertiary_inverse_disabled",
            { s -> s.neutralPalette }
        ) { s -> if (s.isDark) 10.0 else 90.0 }
    }

    // textColorHintInverse documented, in both M3 & GM3, as N10/N90
    fun textHintInverse(): DynamicColor {
        return DynamicColor.fromPalette(
            "text_hint_inverse", { s -> s.neutralPalette }) { s -> if (s.isDark) 10.0 else 90.0 }
    }

    private fun viewingConditionsForAlbers(scheme: DynamicScheme): ViewingConditions {
        return ViewingConditions.defaultWithBackgroundLstar(if (scheme.isDark) 30.0 else 80.0)
    }

    private fun isFidelity(scheme: DynamicScheme): Boolean {
        return scheme.variant === Variant.FIDELITY || scheme.variant === Variant.CONTENT
    }

    private fun isMonochrome(scheme: DynamicScheme): Boolean {
        return scheme.variant === Variant.MONOCHROME
    }

    fun findDesiredChromaByTone(
        hue: Double, chroma: Double, tone: Double, byDecreasingTone: Boolean
    ): Double {
        var answer = tone
        var closestToChroma = Hct.from(hue, chroma, tone)
        if (closestToChroma.chroma < chroma) {
            var chromaPeak = closestToChroma.chroma
            while (closestToChroma.chroma < chroma) {
                answer += if (byDecreasingTone) -1.0 else 1.0
                val potentialSolution = Hct.from(hue, chroma, answer)
                if (chromaPeak > potentialSolution.chroma) {
                    break
                }
                if (abs(potentialSolution.chroma - chroma) < 0.4) {
                    break
                }
                val potentialDelta: Double = abs(potentialSolution.chroma - chroma)
                val currentDelta: Double = abs(closestToChroma.chroma - chroma)
                if (potentialDelta < currentDelta) {
                    closestToChroma = potentialSolution
                }
                chromaPeak = max(chromaPeak, potentialSolution.chroma)
            }
        }
        return answer
    }

    fun performAlbers(prealbers: Hct, scheme: DynamicScheme): Double {
        val albersd = prealbers.inViewingConditions(viewingConditionsForAlbers(scheme))
        return if (DynamicColor.tonePrefersLightForeground(prealbers.tone)
            && !DynamicColor.toneAllowsLightForeground(albersd.tone)
        ) {
            DynamicColor.enableLightForeground(prealbers.tone)
        } else {
            DynamicColor.enableLightForeground(albersd.tone)
        }
    }
}