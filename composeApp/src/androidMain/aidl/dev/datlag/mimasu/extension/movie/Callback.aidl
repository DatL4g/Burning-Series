package dev.datlag.mimasu.extension.movie;

/**
 * Exchanges data between Mimasu and extensions related to movies.
 * Make sure to copy the full directories and files as is!
 *
 * !!! Currently unused !!!
 */
interface Callback {
    void onResult(in byte[] info);
}