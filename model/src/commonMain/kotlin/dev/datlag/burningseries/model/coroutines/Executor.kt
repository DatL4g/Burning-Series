package dev.datlag.burningseries.model.coroutines

import kotlin.jvm.JvmOverloads

class Executor @JvmOverloads constructor(
    private val conflatedExecutor: ConflatedExecutor = ConflatedExecutorImpl(),
    private val queueExecutor: QueueExecutor = QueueExecutorImpl()
) : ConflatedExecutor by conflatedExecutor, QueueExecutor by queueExecutor {

    suspend fun <T> execute(
        schema: Schema,
        block: suspend () -> T
    ): T {
        return when (schema) {
            is Schema.Conflated -> conflate(block)
            is Schema.Queue -> enqueue(block)
        }
    }

    sealed interface Schema {
        data object Conflated : Schema
        data object Queue: Schema
    }
}