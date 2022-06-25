@file:Obfuscate

package de.datlag.burningseries.common

import com.hadiyarajesh.flower.Resource
import de.datlag.burningseries.R
import io.michaelrocks.paranoid.Obfuscate

fun Resource.Status.ERROR.mapToMessageAndDisplayAction(): Pair<Int, Boolean> = when {
    this.statusCode < 100 -> R.string.error_internet_connecton to false
    this.statusCode in 400..499 -> R.string.error_bad_request to true
    this.statusCode in 500..599 -> R.string.error_server_error to true
    else -> R.string.error_could_not_load to true
}