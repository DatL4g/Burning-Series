package dev.datlag.burningseries.network.common

import com.freeletics.mad.statemachine.StateMachine
import dev.datlag.tooling.async.suspendCatching

suspend fun <Action : Any> StateMachine<*, Action>.dispatchIgnoreCollect(action: Action): Boolean = suspendCatching {
    this@dispatchIgnoreCollect.dispatch(action)
}.isSuccess
