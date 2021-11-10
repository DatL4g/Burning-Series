package de.datlag.model.burningseries.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Home(
	val success: Boolean = false,
	val data: HomeData
) : Parcelable