package dev.datlag.burningseries.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class Cacheable<T>(private val timeAlive: Duration = 2.hours) {
    private var cachedOn: Instant? = null
    private var data: T? = null

    fun cache(data: T): T {
        this.data = data
        this.cachedOn = Clock.System.now()
        return data
    }

    fun getAlive(): T? {
        val cacheTime = cachedOn ?: return null

        return if (cacheTime.epochSeconds < Clock.System.now().minus(timeAlive).epochSeconds) {
            null
        } else {
            data
        }
    }

    fun getUnAlive(): T? {
        return data
    }

    operator fun get(unAlive: Boolean = false): T? {
        return if (unAlive) {
            getUnAlive()
        } else {
            getAlive()
        }
    }
}