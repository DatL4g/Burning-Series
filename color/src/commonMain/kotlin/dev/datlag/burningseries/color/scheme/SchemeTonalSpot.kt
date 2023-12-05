package dev.datlag.burningseries.color.scheme

import dev.datlag.burningseries.color.hct.Hct
import dev.datlag.burningseries.color.palettes.TonalPalette
import dev.datlag.burningseries.color.utils.MathUtils.sanitizeDegreesDouble


class SchemeTonalSpot(sourceColorHct: Hct, isDark: Boolean, contrastLevel: Double) : DynamicScheme(
    sourceColorHct,
    Variant.TONAL_SPOT,
    isDark,
    contrastLevel,
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 36.0),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 16.0),
    TonalPalette.fromHueAndChroma(
        sanitizeDegreesDouble(sourceColorHct.hue + 60.0), 24.0
    ),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 6.0),
    TonalPalette.fromHueAndChroma(sourceColorHct.hue, 8.0)
)