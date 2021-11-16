package de.datlag.model.m3o.image

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class Convert(
	@SerialName("base64") val base64: String = String(),
	@SerialName("url") val url: String = String()
) : Parcelable {
	
	@Parcelize
	@Serializable
	@Obfuscate
	data class RequestURL(
		@SerialName("url") val url: String,
		@SerialName("name") val name: String = url.substringAfterLast('/'),
		@SerialName("outputURL") val outputURL: Boolean = false,
	) : Parcelable
	
	@Parcelize
	@Serializable
	@Obfuscate
	data class RequestBase64(
		@SerialName("base64") val base64: String,
		@SerialName("name") val name: String = String(),
		@SerialName("outputURL") val outputURL: Boolean = false,
	) : Parcelable
}
