package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.movie.MovieCallback;
import dev.datlag.mimasu.extension.movie.StreamCallback;

interface IMovieProvider {
    const int VERSION = 1;

    void requestMovieId(in byte[] request, in MovieCallback callback);
    void requestStream(in int movieId, in StreamCallback callback);
}