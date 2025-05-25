package dev.datlag.mimasu.extension.movie;

import dev.datlag.mimasu.extension.movie.WatchInfo;

interface Callback {
    void onResult(in WatchInfo watchInfo);
}