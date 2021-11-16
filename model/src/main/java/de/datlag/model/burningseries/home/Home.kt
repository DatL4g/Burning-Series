package de.datlag.model.burningseries.home

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class Home(
	@SerialName("success") val success: Boolean = false,
	@SerialName("data") val data: HomeData
) : Parcelable