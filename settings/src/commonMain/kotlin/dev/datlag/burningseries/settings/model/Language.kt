package dev.datlag.burningseries.settings.model

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Language.LanguageSerializer::class)
sealed interface Language : Comparable<Language> {
    val code: String

    override fun compareTo(other: Language): Int {
        return when {
            this.code.equals(other.code, ignoreCase = true) -> 0
            this.code.startsWith(other.code, ignoreCase = true) -> 1
            other.code.startsWith(this.code, ignoreCase = true) -> 1
            else -> 2
        }
    }

    fun compareToNullable(other: Language?): Int {
        if (other == null) {
            return 0
        }

        return compareTo(other)
    }

    @Serializable
    sealed interface German : Language {

        @Serializable
        data object Default : German {
            override val code: String = "de"
        }

        @Serializable
        data object Subtitle : German {
            override val code: String = "des"
        }
    }

    @Serializable
    data object English : Language {
        override val code: String = "en"
    }

    @Serializable
    data object JapaneseSubtitle : Language {
        override val code: String = "jps"
    }

    companion object LanguageSerializer : KSerializer<Language?> {
        val all: ImmutableSet<Language> = persistentSetOf(
            German.Default,
            German.Subtitle,
            English,
            JapaneseSubtitle
        )

        fun fromString(value: String?): Language? = when {
            value.equals(German.Default.code, ignoreCase = true) -> German.Default
            value.equals(German.Subtitle.code, ignoreCase = true) -> German.Subtitle
            value.equals(English.code, ignoreCase = true) -> English
            value.equals(JapaneseSubtitle.code, ignoreCase = true) -> JapaneseSubtitle
            else -> null
        }

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Language", PrimitiveKind.STRING)

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder): Language? {
            return if (decoder.decodeNotNullMark()) {
                fromString(decoder.decodeString())
            } else {
                decoder.decodeNull()
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: Language?) {
            if (value != null) {
                encoder.encodeNotNullMark()
                encoder.encodeString(value.code)
            } else {
                encoder.encodeNull()
            }
        }
    }
}