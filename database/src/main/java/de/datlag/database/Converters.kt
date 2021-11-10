package de.datlag.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {
	
	@TypeConverter
	fun toBitmap(bytes: ByteArray): Bitmap? {
		if (bytes.isEmpty()) return null
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
	}
	
	@TypeConverter
	fun fromBitmap(bitmap: Bitmap?): ByteArray {
		if (bitmap == null) return ByteArray(0)
		val outputStream = ByteArrayOutputStream()
		bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
		return outputStream.toByteArray()
	}
}