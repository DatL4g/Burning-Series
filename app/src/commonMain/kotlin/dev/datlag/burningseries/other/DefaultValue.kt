package dev.datlag.burningseries.other

sealed class DefaultValue<T> {
    class INITIAL_LOADING<T>(val data: T?) : DefaultValue<T>()
    class VALUE<T>(val data: T) : DefaultValue<T>()
}