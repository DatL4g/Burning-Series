package dev.datlag.burningseries.network.state

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.datlag.burningseries.model.algorithm.MD5
import dev.datlag.burningseries.model.common.setFrom
import dev.datlag.burningseries.model.state.EpisodeAction
import dev.datlag.burningseries.model.state.EpisodeState
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.scraper.Video
import io.ktor.client.*
import io.realm.kotlin.annotations.ExperimentalRealmSerializerApi
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.ext.call
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.mongodb.kbson.BsonDocument

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodeStateMachine(
    private val client: HttpClient,
    private val jsonBase: JsonBase,
    private val app: App?
) : FlowReduxStateMachine<EpisodeState, EpisodeAction>(initialState = EpisodeState.Waiting) {

    private var user: User? = null
    private val hosterMap: MutableMap<String, List<String>> = mutableMapOf()

    init {
        spec {
            inState<EpisodeState.Waiting> {
                onEnterEffect {
                    if (user == null) {
                        try {
                            user = app?.login(Credentials.anonymous())
                        } catch (ignored: Throwable) { }
                    }
                }
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }

            inState<EpisodeState.Loading> {
                onEnter { state ->
                    val episodeHref = state.snapshot.episode.href
                    val hosterHref = state.snapshot.episode.hosters.map { it.href }

                    val jsonBaseResults = coroutineScope {
                        hosterHref.map { async {
                            try {
                                val entry = jsonBase.burningSeriesCaptcha(MD5.hexString(it))
                                if (!entry.broken) {
                                    entry.url
                                } else {
                                    null
                                }
                            } catch (ignored: Throwable) {
                                null
                            }
                        } }.awaitAll().filterNotNull()
                    }
                    val memoryHoster = hosterMap[episodeHref] ?: emptyList()
                    val mongoDBResults = memoryHoster.ifEmpty {
                        val newList = coroutineScope {
                            try {
                                val doc = user!!.functions.call<BsonDocument>("query", hosterHref.toTypedArray())
                                doc.getArray("result").values.map { it.asDocument().getString("url").value }
                            } catch (ignored: Throwable) {
                                emptyList()
                            }
                        }
                        hosterMap[episodeHref] = newList
                        newList
                    }

                    if (jsonBaseResults.isNotEmpty() || mongoDBResults.isNotEmpty()) {
                        state.override { EpisodeState.SuccessHoster(
                            episode = state.snapshot.episode,
                            results = setFrom(
                                jsonBaseResults,
                                mongoDBResults
                            )
                        ) }
                    } else {
                        state.override { EpisodeState.ErrorHoster(state.snapshot.episode) }
                    }
                }
            }

            inState<EpisodeState.SuccessHoster> {
                onEnter { state ->
                    val urls = state.snapshot.results
                    val streams = coroutineScope {
                        urls.map { async {
                            Video.loadVideos(client, it)
                        } }.awaitAll()
                    }.filterNotNull()

                    if (streams.isEmpty()) {
                        state.override { EpisodeState.ErrorStream(state.snapshot.episode) }
                    } else {
                        state.override { EpisodeState.SuccessStream(streams) }
                    }
                }
            }

            inState<EpisodeState.SuccessStream> {
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }

            inState<EpisodeState.ErrorHoster> {
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }

            inState<EpisodeState.ErrorStream> {
                on<EpisodeAction.Load> { action, state ->
                    state.override { EpisodeState.Loading(action.episode) }
                }
            }
        }
    }
}