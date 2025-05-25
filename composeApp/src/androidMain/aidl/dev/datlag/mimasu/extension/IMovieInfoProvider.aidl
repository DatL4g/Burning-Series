package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.movie.Callback;
import dev.datlag.mimasu.extension.movie.Request;

interface IMovieInfoProvider {
    const int VERSION = 1;

    void requestInfo(in Request request, in Callback callback);
}