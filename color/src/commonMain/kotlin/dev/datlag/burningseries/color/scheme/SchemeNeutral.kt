package dev.datlag.burningseries.color.scheme

import dev.datlag.burningseries.color.hct.Hct
import dev.datlag.burningseries.color.palettes.TonalPalette


class SchemeNeutral(sourceColorHct: Hct, isDark: Boolean, contrastLevel: Double) :
    DynamicScheme(
        sourceColorHct,
        Variant.NEUTRAL,
        isDark,
        contrastLevel,
        TonalPalette.fromHueAndChroma(sourceColorHct.hue, 12.0),
        TonalPalette.fromHueAndChroma(sourceColorHct.hue, 8.0),
        TonalPalette.fromHueAndChroma(sourceColorHct.hue, 16.0),
        TonalPalette.fromHueAndChroma(sourceColorHct.hue, 2.0),
        TonalPalette.fromHueAndChroma(sourceColorHct.hue, 2.0)
    )