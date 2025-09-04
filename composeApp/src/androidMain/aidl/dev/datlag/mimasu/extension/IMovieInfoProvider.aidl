package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.movie.Callback;

/**
 * Exchanges data between Mimasu and extensions related to movies.
 * Make sure to copy the full directories and files as is!
 *
 * !!! Currently unused !!!
 */
interface IMovieInfoProvider {
    const int VERSION = 1;

    void requestInfo(in byte[] request, in Callback callback);
}