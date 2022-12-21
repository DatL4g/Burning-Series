package dev.datlag.burningseries.network

import com.hadiyarajesh.flower_core.Resource

sealed class Status {
    object LOADING : Status()
    sealed class ERROR : Status() {
        object TOO_MANY_REQUESTS : ERROR()

        object BAD_REQUEST : ERROR()

        object INTERNAL : ERROR()

        object CLIENT : ERROR()

        object SERVER : ERROR()

        companion object {
            fun create(statusCode: Int): ERROR {
                return when (statusCode) {
                    429 -> TOO_MANY_REQUESTS
                    400 -> BAD_REQUEST
                    in 400..499 -> CLIENT
                    in 500..599 -> SERVER
                    else -> INTERNAL
                }
            }
        }
    }
    object SUCCESS : Status()

    companion object {
        fun create(status: Resource.Status<*>, emptySuccessAsError: Boolean = false): Status {
            return when (status) {
                is Resource.Status.Loading -> LOADING
                is Resource.Status.Error -> ERROR.create(status.statusCode)
                is Resource.Status.Success -> SUCCESS
                is Resource.Status.EmptySuccess -> if (emptySuccessAsError) ERROR.create(204) else SUCCESS
            }
        }
    }
}