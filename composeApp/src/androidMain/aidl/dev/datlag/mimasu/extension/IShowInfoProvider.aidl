package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.show.ShowCallback;
import dev.datlag.mimasu.extension.show.EpisodeCallback;
import dev.datlag.mimasu.extension.show.StreamCallback;

interface IShowInfoProvider {
    const int VERSION = 1;

    void requestShowId(in byte[] request, in ShowCallback callback);
    void requestEpisodeAvailability(in int showId, in byte[] request, in EpisodeCallback callback);
    void requestStream(in int showId, in byte[] request, in StreamCallback callback);
}