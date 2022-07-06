package de.datlag.model.common

import de.datlag.model.burningseries.stream.StreamClip
import de.datlag.model.burningseries.stream.StreamConfig
import de.datlag.model.video.VideoStream
import kotlin.math.abs

fun Collection<Long>.closestTo(value: Long) = this.minByOrNull { abs(value - it) }
fun Collection<Long>.closestToAverage(): Long? {
    val average = this.average()
    return if (average.isNaN()) {
        null
    } else {
        closestTo(average.toLong())
    }
}

fun Collection<VideoStream>.getBestConfig(selected: VideoStream?): StreamConfig {
    val preferredIndex = this.indexOf(selected)
    val preferred = if (preferredIndex >= 0) {
        this.elementAt(preferredIndex)
    } else {
        this.firstOrNull { it == selected }
    }

    val throwBackStart = preferred?.config?.throwback?.start ?: this.mapNotNull { it.config.throwback.start }.closestToAverage()
    val throwBackEnd = preferred?.config?.throwback?.end ?: this.mapNotNull { it.config.throwback.end }.closestToAverage()

    val introStart = preferred?.config?.intro?.start ?: this.mapNotNull { it.config.intro.start }.closestToAverage()
    val introEnd = preferred?.config?.intro?.end ?: this.mapNotNull { it.config.intro.end }.closestToAverage()

    val outroStart = preferred?.config?.outro?.start ?: this.mapNotNull { it.config.outro.start }.closestToAverage()
    val outroEnd = preferred?.config?.outro?.end ?: this.mapNotNull { it.config.outro.end }.closestToAverage()

    return StreamConfig(
        StreamClip(
            throwBackStart,
            throwBackEnd
        ),
        StreamClip(
            introStart,
            introEnd
        ),
        StreamClip(
            outroStart,
            outroEnd
        )
    )
}