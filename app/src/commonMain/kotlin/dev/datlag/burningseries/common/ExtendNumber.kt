package dev.datlag.burningseries.common

fun Long.toDuration(): String {
    val duration = this / 1000
    val hours = duration / 3600
    val minutes = (duration - hours * 3600) / 60
    val seconds = duration - (hours * 3600 + minutes * 60)
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours.toInt(), minutes.toInt(), seconds.toInt())
    } else {
        "%02d:%02d".format(minutes.toInt(), seconds.toInt())
    }
}