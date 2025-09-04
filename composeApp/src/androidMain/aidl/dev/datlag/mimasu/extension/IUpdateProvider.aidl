package dev.datlag.mimasu.extension;

import dev.datlag.mimasu.extension.update.Callback;

/**
 * !!! Everything update related can be ignored, not used unless for this specific extension !!!
 *
 * Exchanges data between Mimasu and extensions related to shows.
 * Make sure to copy the full directories and files as is!
 */
interface IUpdateProvider {
    const int VERSION = 1;

    void requestUpdate(in Callback callback);
}