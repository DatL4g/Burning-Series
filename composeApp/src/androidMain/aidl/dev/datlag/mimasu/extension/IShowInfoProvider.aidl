package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.show.Callback;

interface IShowInfoProvider {
    const int VERSION = 1;

    void requestInfo(in byte[] request, in Callback callback);
}