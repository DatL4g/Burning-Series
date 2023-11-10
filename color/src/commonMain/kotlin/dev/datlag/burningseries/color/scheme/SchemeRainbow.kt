package dev.datlag.burningseries.color.scheme

import dev.datlag.burningseries.color.hct.Hct
import dev.datlag.burningseries.color.palettes.TonalPalette
import dev.datlag.burningseries.color.utils.MathUtils

class SchemeRainbow(
    sourceColorHct: Hct,
    isDark: Boolean,
    contrastLevel: Double
) : DynamicScheme(
    sourceColorHct,
    Variant.RAINBOW,
    isDark,
    contrastLevel,
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 48.0),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 16.0),
    TonalPalette.fromHueAndChroma(
        MathUtils.sanitizeDegreesDouble(sourceColorHct.hue + 60), 24.0
    ),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 0.0),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 0.0)
)