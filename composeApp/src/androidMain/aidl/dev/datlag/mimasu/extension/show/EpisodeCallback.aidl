package dev.datlag.mimasu.extension.show;

/**
 * Exchanges data between Mimasu and extensions related to shows.
 * Make sure to copy the full directories and files as is!
 */
interface EpisodeCallback {
    void onResult(in boolean available);
}