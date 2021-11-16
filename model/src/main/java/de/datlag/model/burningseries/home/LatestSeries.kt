package de.datlag.model.burningseries.home

import android.os.Parcelable
import de.datlag.model.burningseries.common.encodeToHref
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class LatestSeries(
	@SerialName("title") val title: String = String(),
	@SerialName("href") val href: String = String()
) : Parcelable {

	fun getHrefTitle() = title.encodeToHref()
}
