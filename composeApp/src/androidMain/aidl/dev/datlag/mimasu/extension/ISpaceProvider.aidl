package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.space.SpaceCallback;

interface ISpaceProvider {
    const int VERSION = 1;

    void requestSpace(in SpaceCallback callback);
    boolean clearCache(in SpaceCallback callback);
    boolean clearStorage(in SpaceCallback callback);
}