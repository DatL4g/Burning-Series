package dev.datlag.burningseries.color

import dev.datlag.burningseries.color.theme.CustomColor
import dev.datlag.burningseries.color.theme.Theme
import dev.datlag.burningseries.color.utils.ThemeUtils
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt

fun Image.toBufferedImage(): BufferedImage {
    if (this is BufferedImage) {
        return this
    }
    val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)

    val graphics2D = bufferedImage.createGraphics()
    graphics2D.drawImage(this, 0, 0, null)
    graphics2D.dispose()

    return bufferedImage
}

fun ThemeUtils.themeFromImage(image: Image, vararg customColors: CustomColor = emptyArray()): Theme {
    val img = image.toBufferedImage()
    val pixels = (img.raster.dataBuffer as? DataBufferByte)?.data ?: (img.raster.getDataElements(0, 0, img.width, img.height, null) as? ByteArray)
    val hasAlphaChannel = img.alphaRaster != null

    return pixels?.let {
        byteArrayToTheme(it, hasAlphaChannel, *customColors)
    } ?: intArrayToTheme((img.raster.dataBuffer as DataBufferInt).data, *customColors)
}

fun Image.createTheme(vararg customColors: CustomColor = emptyArray()) = ThemeUtils.themeFromImage(this, *customColors)

fun Image.getMainColor(): Int {
    val img = this.toBufferedImage()
    val pixels = (img.raster.dataBuffer as? DataBufferByte)?.data ?: (img.raster.getDataElements(0, 0, img.width, img.height, null) as? ByteArray)
    val hasAlphaChannel = img.alphaRaster != null

    return pixels?.let {
        ThemeUtils.byteArrayMainColor(it, hasAlphaChannel)
    } ?: ThemeUtils.intArrayMainColor((img.raster.dataBuffer as DataBufferInt).data)
}
