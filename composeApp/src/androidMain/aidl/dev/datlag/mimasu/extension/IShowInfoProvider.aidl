package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.show.ShowCallback;
import dev.datlag.mimasu.extension.show.EpisodeCallback;
import dev.datlag.mimasu.extension.show.StreamCallback;

/**
 * Exchanges data between Mimasu and extensions related to shows.
 * Make sure to copy the full directories and files as is!
 */
interface IShowInfoProvider {
    const int VERSION = 1;

    /** Checks extension for a unique id for the given show */
    void requestShowId(in byte[] request, in ShowCallback callback);

    /** Checks extension whether an episode for the given show id and data is available */
    void requestEpisodeAvailability(in int showId, in byte[] request, in EpisodeCallback callback);

    /** Requests extension streams for the given show id and episode data */
    void requestStream(in int showId, in byte[] request, in StreamCallback callback);
}