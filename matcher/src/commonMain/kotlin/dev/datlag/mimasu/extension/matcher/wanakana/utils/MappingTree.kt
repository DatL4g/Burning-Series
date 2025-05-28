package dev.datlag.mimasu.extension.matcher.wanakana.utils

import dev.datlag.mimasu.extension.matcher.wanakana.common.mappingTreeOf
import dev.datlag.mimasu.extension.matcher.wanakana.common.mutableMappingTreeOf

internal sealed class MappingTree {

    abstract val value: String?

    abstract val subTrees: Map<Char, MappingTree>?

    open operator fun get(key: Char): MappingTree? = subTrees?.get(key)

    abstract fun hasSubTree(): Boolean

    abstract fun toMutable(): Mutable

    abstract fun toImmutable(): Immutable

    abstract fun duplicate(): MappingTree

    open fun merge(other: MappingTree): MappingTree {
        return other.duplicate().toMutable().also { this.mergeInto(it) }
    }

    open fun mergeInto(other: Mutable) {
        other.value = value
        if (!hasSubTree()) {
            return
        }

        subTrees?.forEach { (char, subTree) ->
            val otherSubTree = other[char]
            if (otherSubTree == null) {
                other[char] = subTree.toMutable()
            } else {
                subTree.mergeInto(otherSubTree)
            }
        }
    }

    data class Immutable(
        val children: Map<Char, MappingTree>? = null,
        override val value: String? = null
    ) : MappingTree() {

        override val subTrees: Map<Char, MappingTree>? = children

        override fun hasSubTree(): Boolean = children?.isNotEmpty() == true

        override fun duplicate(): MappingTree {
            return this.copy()
        }

        override fun toMutable(): Mutable {
            return mutableMappingTreeOf(value = value).also { newTree ->
                children?.forEach { (key, value) ->
                    newTree[key] = value.toMutable()
                }
            }
        }

        override fun toImmutable(): Immutable {
            return this
        }
    }

    data class Mutable(
        override var value: String? = null
    ) : MappingTree() {

        private val childrenDelegate = lazy { mutableMapOf<Char, Mutable>() }
        private val children: MutableMap<Char, Mutable> by childrenDelegate

        override fun hasSubTree(): Boolean {
            return childrenDelegate.isInitialized() && children.isNotEmpty()
        }

        override val subTrees: MutableMap<Char, Mutable>
            get() = children

        override operator fun get(key: Char): Mutable? = subTrees[key]

        operator fun set(key: Char, subTree: Mutable) {
            subTrees[key] = subTree
        }

        fun subTreeOf(str: String): Mutable {
            if (str.isEmpty()) {
                return this
            }
            if (str.length == 1) {
                val char = str.first()
                return children[char] ?: mutableMappingTreeOf().also { children[char] = it }
            }
            return str.toCharArray().fold<Mutable>(initial = this) { result, char ->
                result[char] ?: mutableMappingTreeOf().also { result[char] = it }
            }
        }

        fun replaceSubTreeOf(str: String, newSubTree: Mutable) {
            if (str.isEmpty()) {
                throw IllegalArgumentException("An empty string is not a valid subTree reference.")
            }
            val lastChar = str.last()
            val toUpdate = subTreeOf(str.dropLast(1))
            toUpdate[lastChar] = newSubTree
        }

        fun setSubTreeValue(str: String, value: String) {
            val toUpdate = subTreeOf(str)
            toUpdate.value = value
        }

        override fun duplicate(): Mutable {
            return mutableMappingTreeOf(value = value).also { newTree ->
                children.forEach { (char, subTree) ->
                    newTree[char] = subTree.duplicate()
                }
            }
        }

        override fun toMutable(): Mutable {
            return this
        }

        override fun toImmutable(): Immutable {
            return mappingTreeOf(
                value = value,
                subTrees = children.mapValues { (_, value) -> value.toImmutable() }
            )
        }
    }

    class Builder(
        val tree: Mutable = mutableMappingTreeOf()
    ) {

        var value: String?
            get() = tree.value
            set(value) { tree.value = value }

        fun value(value: String?) = apply {
            this.value = value
        }

        infix fun Char.to(value: String) = apply {
            tree.setSubTreeValue(this.toString(), value)
        }

        infix fun String.to(value: String) = apply {
            if (this.isEmpty()) {
                throw IllegalArgumentException("An empty string is not a valid subTree reference.")
            }
            tree.setSubTreeValue(this, value)
        }

        infix fun Char.to(value: Char) = apply {
            tree[this] = mutableMappingTreeOf(value = value.toString())
        }

        infix fun String.to(value: Char) = apply {
            if (this.isEmpty()) {
                throw IllegalArgumentException("An empty string is not a valid subTree reference.")
            }
            tree.setSubTreeValue(this, value.toString())
        }

        infix fun Char.to(subTree: Mutable) = apply {
            tree[this] = subTree
        }

        infix fun String.to(subTree: Mutable) = apply {
            if (this.isEmpty()) {
                throw IllegalArgumentException("An empty string is not a valid subTree reference.")
            }
            tree.replaceSubTreeOf(this, subTree)
        }

        infix fun Char.merge(subTree: Mutable) = apply {
            subTree.mergeInto(tree.subTreeOf(this.toString()))
        }

        infix fun String.merge(subTree: Mutable) = apply {
            if (this.isEmpty()) {
                throw IllegalArgumentException("An empty string is not a valid subTree reference.")
            }
            subTree.mergeInto(tree.subTreeOf(this))
        }

        fun build(): Mutable = tree
    }
}