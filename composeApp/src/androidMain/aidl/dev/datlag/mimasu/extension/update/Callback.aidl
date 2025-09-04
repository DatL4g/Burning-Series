package dev.datlag.mimasu.extension.update;

/**
 * !!! Everything update related can be ignored, not used unless for this specific extension !!!
 *
 * Exchanges data between Mimasu and extensions related to shows.
 * Make sure to copy the full directories and files as is!
 */
interface Callback {

    void onResult(in byte[] info);

}