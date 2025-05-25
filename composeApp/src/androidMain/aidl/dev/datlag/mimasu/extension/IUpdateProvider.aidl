package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.update.Callback;

interface IUpdateProvider {
    const int VERSION = 1;

    void requestUpdate(in Callback callback);
}