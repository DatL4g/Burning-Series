package dev.datlag.burningseries.color.scheme

import dev.datlag.burningseries.color.dislike.DislikeAnalyzer.fixIfDisliked
import dev.datlag.burningseries.color.hct.Hct
import dev.datlag.burningseries.color.palettes.TonalPalette
import dev.datlag.burningseries.color.temperature.TemperatureCache
import kotlin.math.max


class SchemeContent(sourceColorHct: Hct, isDark: Boolean, contrastLevel: Double) :
    DynamicScheme(
        sourceColorHct,
        Variant.CONTENT,
        isDark,
        contrastLevel,
        TonalPalette.fromHueAndChroma(sourceColorHct.hue, sourceColorHct.chroma),
        TonalPalette.fromHueAndChroma(
            sourceColorHct.hue,
            max(sourceColorHct.chroma - 32.0, sourceColorHct.chroma * 0.5)
        ),
        TonalPalette.fromHct(
            fixIfDisliked(
                TemperatureCache(sourceColorHct)
                    .getAnalogousColors( /* count= */3,  /* divisions= */6)[2]
            )
        ),
        TonalPalette.fromHueAndChroma(sourceColorHct.hue, sourceColorHct.chroma / 8.0),
        TonalPalette.fromHueAndChroma(
            sourceColorHct.hue, sourceColorHct.chroma / 8.0 + 4.0
        )
    )