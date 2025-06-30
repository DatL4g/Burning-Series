package dev.datlag.mimasu.extension.ui.theme

import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import dev.datlag.mimasu.extension.R

data object Font {

    private const val MANROPE = "Manrope"

    private val googleProvider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    private val manropeFont = GoogleFont(MANROPE)
    val manrope = FontFamily(
        Font(manropeFont, googleProvider, FontWeight.ExtraLight),
        Font(manropeFont, googleProvider, FontWeight.ExtraLight, FontStyle.Italic),

        Font(manropeFont, googleProvider, FontWeight.Light),
        Font(manropeFont, googleProvider, FontWeight.Light, FontStyle.Italic),

        Font(manropeFont, googleProvider, FontWeight.Normal),
        Font(manropeFont, googleProvider, FontWeight.Normal, FontStyle.Italic),

        Font(manropeFont, googleProvider, FontWeight.Medium),
        Font(manropeFont, googleProvider, FontWeight.Medium, FontStyle.Italic),

        Font(manropeFont, googleProvider, FontWeight.SemiBold),
        Font(manropeFont, googleProvider, FontWeight.SemiBold, FontStyle.Italic),

        Font(manropeFont, googleProvider, FontWeight.Bold),
        Font(manropeFont, googleProvider, FontWeight.Bold, FontStyle.Italic),

        Font(manropeFont, googleProvider, FontWeight.ExtraBold),
        Font(manropeFont, googleProvider, FontWeight.ExtraBold, FontStyle.Italic),
    )
}