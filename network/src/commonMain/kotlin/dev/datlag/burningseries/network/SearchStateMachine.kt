package dev.datlag.burningseries.network

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.firebase.FirebaseFactory
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.network.state.SearchAction
import dev.datlag.burningseries.network.state.SearchState
import dev.datlag.tooling.async.suspendCatching
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalCoroutinesApi::class)
class SearchStateMachine(
    private val client: HttpClient,
    private val crashlytics: FirebaseFactory.Crashlytics?
) : FlowReduxStateMachine<SearchState, SearchAction>(
    initialState = currentState
) {

    var currentState: SearchState
        get() = Companion.currentState
        private set(value) {
            Companion.currentState = value
        }

    init {
        spec {
            inState<SearchState> {
                onEnterEffect {
                    currentState = it
                }
            }
            inState<SearchState.Loading> {
                onEnter { state ->
                    val result = suspendCatching {
                        BurningSeries.search(client)
                    }

                    state.override {
                        SearchState.fromResult(result)
                    }
                }
            }
            inState<SearchState.Failure> {
                onEnterEffect {
                    crashlytics?.log(it.throwable)
                }
                on<SearchAction.Retry> { _, state ->
                    state.override { SearchState.Loading }
                }
            }
            inState<SearchState.Success> {
                on<SearchAction.Query> { action, state ->
                    val search = if (action.query.isNullOrBlank()) {
                        persistentSetOf()
                    } else {
                        coroutineScope {
                            state.snapshot.allItems.map {
                                async {
                                    when {
                                        it.title.equals(action.query, ignoreCase = true) -> it to 1.0
                                        it.mainTitle.equals(action.query, ignoreCase = true) -> it to 0.99
                                        it.subTitle.equals(action.query, ignoreCase = true) -> it to 0.96
                                        it.title.startsWith(action.query, ignoreCase = true) -> it to 0.95
                                        it.title.contains(action.query, ignoreCase = true) -> it to 0.9
                                        else -> it to JaroWinkler.distance(it.title, action.query)
                                    }
                                }
                            }.awaitAll().filter {
                                it.second > 0.85
                            }.sortedByDescending { it.second }.map { it.first }.toImmutableSet()
                        }
                    }

                    state.mutate {
                        copy(queriedItems = search)
                    }
                }
            }
        }
    }

    companion object {
        var currentState: SearchState
            get() = StateSaver.searchState
            private set(value) {
                StateSaver.searchState = value
            }
    }
}