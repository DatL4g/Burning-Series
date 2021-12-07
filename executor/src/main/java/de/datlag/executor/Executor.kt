package de.datlag.executor

import de.datlag.executor.conflated.ConflatedExecutor
import de.datlag.executor.conflated.ConflatedExecutorInterface
import de.datlag.executor.queue.QueueExecutor
import de.datlag.executor.queue.QueueExecutorInterface
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class Executor constructor(
    conflatedExecutor: ConflatedExecutorInterface,
    queueExecutor: QueueExecutorInterface
) : ExecutorInterface,
    ConflatedExecutorInterface by conflatedExecutor,
    QueueExecutorInterface by queueExecutor {

    constructor() : this(
        ConflatedExecutor(),
        QueueExecutor()
    )

    override suspend fun <T> execute(schema: Schema, block: suspend () -> T): T {
        return when (schema) {
            is Schema.Conflated -> conflate(block)
            is Schema.Queue -> enqueue(block)
        }
    }
}