package dev.datlag.burningseries.color.scheme

import dev.datlag.burningseries.color.hct.Hct
import dev.datlag.burningseries.color.palettes.TonalPalette
import dev.datlag.burningseries.color.utils.MathUtils

class SchemeFruitSalad(
    sourceColorHct: Hct,
    isDark: Boolean,
    contrastLevel: Double
) : DynamicScheme(
    sourceColorHct,
    Variant.FRUIT_SALAD,
    isDark,
    contrastLevel,
    TonalPalette.fromHueAndChroma(
        MathUtils.sanitizeDegreesDouble(sourceColorHct.hue - 50.0), 48.0
    ),
    TonalPalette.fromHueAndChroma(
        MathUtils.sanitizeDegreesDouble(sourceColorHct.hue - 50.0), 36.0
    ),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 36.0),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 10.0),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 16.0)
)