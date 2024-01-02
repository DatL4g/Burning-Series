package dev.datlag.burningseries.model.state


sealed interface CatchResult<T> {

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    fun onError(callback: (Throwable?) -> Unit) = apply {
        if (this is Error) {
            callback(this.throwable)
        }
    }

    fun onSuccess(callback: (T & Any) -> Unit) = apply {
        if (this is Success) {
            callback(this.data)
        }
    }

    fun asSuccess(onError: () -> T & Any): T & Any {
        return if (this is Success) {
            this.data
        } else {
            onError()
        }
    }

    fun asNullableSuccess(onError: () -> T? = { null }): T? {
        return if (this is Success) {
            this.data
        } else {
            onError()
        }
    }

    fun asError(onSuccess: () -> Throwable? = { null }): Throwable? {
        return if (this is Error) {
            this.throwable
        } else {
            onSuccess()
        }
    }

    data class Success<T>(
        val data: T & Any
    ) : CatchResult<T & Any>
    data class Error<T>(val throwable: Throwable?) : CatchResult<T>
}