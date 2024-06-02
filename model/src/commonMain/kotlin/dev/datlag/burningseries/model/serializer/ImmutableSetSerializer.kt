package dev.datlag.burningseries.model.serializer

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerializableImmutableSet<T> = @Serializable(ImmutableSetSerializer::class) ImmutableSet<T>

@Serializer(forClass = ImmutableSet::class)
class ImmutableSetSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<ImmutableSet<T>> {
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<String>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.ImmutableSet"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()
    override fun serialize(encoder: Encoder, value: ImmutableSet<T>) {
        return SetSerializer(dataSerializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ImmutableSet<T> {
        return SetSerializer(dataSerializer).deserialize(decoder).toPersistentSet()
    }
}