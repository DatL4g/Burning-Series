package dev.datlag.burningseries.color

import dev.datlag.burningseries.color.theme.CustomColor
import dev.datlag.burningseries.color.theme.Theme
import dev.datlag.burningseries.color.utils.ColorUtils
import dev.datlag.burningseries.color.utils.ThemeUtils
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.get
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.get
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

suspend fun ThemeUtils.themeFromImage(image: HTMLImageElement, vararg customColors: CustomColor = emptyArray()) : Theme = suspendCoroutine { continuation ->
    Promise<IntArray> { resolver, rejector ->
        val canvas = document.createElement("canvas").unsafeCast<HTMLCanvasElement?>()
        val context = canvas?.getContext("2d")

        if (context == null) {
            rejector(Error("Could not get canvas context"))
            return@Promise
        }

        fun callback() {
            canvas.width = image.width
            canvas.height = image.height
            context.asDynamic().drawImage(image, 0, 0)

            val rect = arrayOf(0, 0, image.width, image.height)
            val data = context.asDynamic().getImageData(rect[0], rect[1], rect[2], rect[3]).data
            val casted = (data as Uint8ClampedArray).toIntArray()
            resolver(casted)
        }

        if (image.complete) {
            callback()
        } else {
            image.onload = {
                callback()
            }
        }
    }.then { data ->
        val pixels = mutableListOf<Int>()
        for (i in data.indices step 4) {
            val r = data[i]
            val g = data[i + 1]
            val b = data[i + 2]
            val a = data[i + 3]
            if (a < 255) {
                continue
            }
            pixels.add(ColorUtils.argbFromRgb(r, g, b))
        }
        continuation.resume(intArrayToTheme(pixels.toIntArray(), *customColors))
    }.catch {
        continuation.resumeWithException(it)
    }
}

private fun Uint8ClampedArray.toIntArray(): IntArray {
    val array = IntArray(this.length)
    for (i in 0 until this.length) {
        array[i] = this[i].toInt()
    }
    return array
}

suspend fun HTMLImageElement.createTheme(vararg customColors: CustomColor = emptyArray()) = ThemeUtils.themeFromImage(this, *customColors)