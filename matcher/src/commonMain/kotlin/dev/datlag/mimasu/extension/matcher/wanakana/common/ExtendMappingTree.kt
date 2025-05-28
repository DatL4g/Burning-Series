package dev.datlag.mimasu.extension.matcher.wanakana.common

import dev.datlag.mimasu.extension.matcher.wanakana.utils.MappingTree

internal fun mappingTreeOf(
    value: String? = null,
    subTrees: Map<Char, MappingTree>? = null
): MappingTree.Immutable = MappingTree.Immutable(
    value = value,
    children = subTrees
)

internal fun mutableMappingTreeOf(
    value: String? = null,
    subTrees: Map<Char, MappingTree.Mutable>? = null
): MappingTree.Mutable = MappingTree.Mutable(value = value)

internal fun mapping(block: MappingTree.Builder.() -> Unit): MappingTree.Mutable {
    return MappingTree.Builder().apply(block).build()
}