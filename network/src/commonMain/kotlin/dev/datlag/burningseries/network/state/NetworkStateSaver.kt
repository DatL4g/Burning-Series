package dev.datlag.burningseries.network.state

import dev.datlag.burningseries.model.Cacheable
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.Release
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.model.state.ReleaseState
import dev.datlag.burningseries.model.state.SearchState
import dev.gitlive.firebase.auth.FirebaseUser

data object NetworkStateSaver {

    var firebaseUser: FirebaseUser? = null

    val mongoHosterMap: MutableMap<String, List<String>> = mutableMapOf()

    // ignore EpisodeState as it's not provided as singleton
    // ignore SaveState as it should reset after clearing view
    // ignore SeriesState as it should reset after clearing view
    var initialHomeState: HomeState = HomeState.Loading
    var initialReleaseState: ReleaseState = ReleaseState.Loading
    var initialSearchState: SearchState = SearchState.Loading

    internal data object Cache {
        val home: Cacheable<Pair<Home, Boolean>> = Cacheable()
        val release: Cacheable<List<Release>> = Cacheable()
        val search: Cacheable<List<Genre>> = Cacheable()
    }
}