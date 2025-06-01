package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.show.ShowCallback;
import dev.datlag.mimasu.extension.show.EpisodeCallback;

interface IShowInfoProvider {
    const int VERSION = 1;

    void requestShowId(in byte[] request, in ShowCallback callback);
    void requestEpisode(in int showId, in byte[] request, in EpisodeCallback callback);
}