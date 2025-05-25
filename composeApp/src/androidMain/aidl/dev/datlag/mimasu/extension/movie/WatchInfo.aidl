package dev.datlag.mimasu.extension.movie;

interface WatchInfo {
    List<String> getSources();

    /**
    * AIDL does not allow integer here.
    * Key is String of language code.
    * Value is int (as String) of position in getSources.
    */
    Map<String, String> getLanguageSourceMapping();
}