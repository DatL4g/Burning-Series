package dev.datlag.burningseries.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class FirestoreQuery(
    @SerialName("structuredQuery") val structuredQuery: StructuredQuery
) {
    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class Value(
        @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("stringValue") val stringValue: String? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("booleanValue") val booleanValue: Boolean? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("integerValue") val integerValue: Int? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("doubleValue") val doubleValue: Double? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("arrayValue") val arrayValue: ArrayValue? = null
    ) {
        @Serializable
        data class ArrayValue(
            @SerialName("values") val values: List<Value>
        )
    }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class StructuredQuery(
        @SerialName("from") val from: List<From>,
        @SerialName("where") val where: Where,
        @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("select") val select: Select? = null
    ) {

        @Serializable
        data class From(
            @SerialName("collectionId") val collectionId: String,
            @SerialName("allDescendants") val allDescendants: Boolean = false
        )

        @Serializable
        data class Where(
            @SerialName("fieldFilter") val fieldFilter: FieldFilter
        ) {

            @Serializable
            data class FieldFilter internal constructor(
                @SerialName("field") val field: Field,
                @SerialName("op") val op: String,
                @SerialName("value") val value: Value
            ) {

                constructor(
                    field: Field,
                    op: OP,
                    value: Value
                ) : this(field, op.name, value)

                sealed class OP(val name: String) {
                    data object OPERATOR_UNSPECIFIED : OP("OPERATOR_UNSPECIFIED")
                    data object LESS_THAN : OP("LESS_THAN")
                    data object LESS_THAN_OR_EQUAL : OP("LESS_THAN_OR_EQUAL")
                    data object GREATER_THAN : OP("GREATER_THAN")
                    data object GREATER_THAN_OR_EQUAL : OP("GREATER_THAN_OR_EQUAL")
                    data object EQUAL : OP("EQUAL")
                    data object NOT_EQUAL : OP("NOT_EQUAL")
                    data object ARRAY_CONTAINS : OP("ARRAY_CONTAINS")
                    data object IN : OP("IN")
                    data object ARRAY_CONTAINS_ANY : OP("ARRAY_CONTAINS_ANY")
                    data object NOT_IN : OP("NOT_IN")
                }
            }
        }

        @Serializable
        data class Select(
            @SerialName("fields") val fields: List<Field>
        )

        @Serializable
        data class Field(
            @SerialName("fieldPath") val fieldPath: String
        )
    }

    @Serializable
    data class Response(
        @SerialName("document") val document: FirestoreDocument
    )
}
