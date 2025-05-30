package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.movie.Callback;

interface IMovieInfoProvider {
    const int VERSION = 1;

    void requestInfo(in byte[] request, in Callback callback);
}