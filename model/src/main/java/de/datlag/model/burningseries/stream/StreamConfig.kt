package de.datlag.model.burningseries.stream

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class StreamConfig(
    @SerialName("throwback") val throwback: StreamClip = StreamClip(),
    @SerialName("intro") val intro: StreamClip = StreamClip(),
    @SerialName("outro") val outro: StreamClip = StreamClip()
) : Parcelable {
    fun isValidChanged(): Boolean {
        return (throwback.start != null && throwback.end != null) || (intro.start != null && intro.end != null) || (outro.start != null && outro.end != null)
    }

    fun combineToValid(config: StreamConfig) = apply {
        val newThrowbackStart = throwback.start ?: config.throwback.start
        val newThrowbackEnd = throwback.end ?: config.throwback.end
        if (newThrowbackStart != null && newThrowbackEnd != null && newThrowbackStart > newThrowbackEnd) {
            throwback.start = config.throwback.start
            throwback.end = config.throwback.end
        } else {
            if (newThrowbackStart == null || newThrowbackEnd == null) {
                throwback.start = config.throwback.start
                throwback.end = config.throwback.end
            } else {
                throwback.start = newThrowbackStart
                throwback.end = newThrowbackEnd
            }
        }

        val newIntroStart = intro.start ?: config.intro.start
        val newIntroEnd = intro.end ?: config.intro.end
        if (newIntroStart != null && newIntroEnd != null && newIntroStart > newIntroEnd) {
            intro.start = config.intro.start
            intro.end = config.intro.end
        } else {
            if (newIntroStart == null || newIntroEnd == null) {
                intro.start = config.intro.start
                intro.end = config.intro.end
            } else {
                intro.start = newIntroStart
                intro.end = newIntroEnd
            }
        }

        val newOutroStart = outro.start ?: config.outro.start
        val newOutroEnd = outro.end ?: config.outro.end
        if (newOutroStart != null && newOutroEnd != null && newOutroStart > newOutroEnd) {
            outro.start = config.outro.start
            outro.end = config.outro.end
        } else {
            if (newOutroStart == null || newOutroEnd == null) {
                outro.start = config.outro.start
                outro.end = config.outro.end
            } else {
                outro.start = newOutroStart
                outro.end = newOutroEnd
            }
        }
    }

    fun newInstance(): StreamConfig {
        return StreamConfig(
            StreamClip(
                this.throwback.start,
                this.throwback.end
            ),
            StreamClip(
                this.intro.start,
                this.intro.end
            ),
            StreamClip(
                this.outro.start,
                this.outro.end
            )
        )
    }

    fun isNewEntry(config: StreamConfig): Boolean {
        return this != config
                && this.hashCode() != config.hashCode()
                && config.throwback.start == null
                && config.throwback.end == null
                && config.intro.start == null
                && config.intro.end == null
                && config.outro.start == null
                && config.outro.end == null
    }
}
