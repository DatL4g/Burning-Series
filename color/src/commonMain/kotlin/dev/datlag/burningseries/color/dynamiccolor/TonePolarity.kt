package dev.datlag.burningseries.color.dynamiccolor

/**
 * Describes the relationship in lightness between two colors.
 *
 * <p>'nearer' and 'farther' describes closeness to the surface roles. For instance,
 * ToneDeltaPair(A, B, 10, 'nearer', stayTogether) states that A should be 10 lighter than B in
 * light mode, and 10 darker than B in dark mode.
 *
 * <p>See `ToneDeltaPair` for details.
 */
enum class TonePolarity {
    DARKER,
    LIGHTER,
    NO_PREFERENCE,
    NEARER,
    FARTHER
}