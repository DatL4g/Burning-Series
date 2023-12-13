package dev.datlag.burningseries.network.state

import dev.datlag.burningseries.model.state.*
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
}