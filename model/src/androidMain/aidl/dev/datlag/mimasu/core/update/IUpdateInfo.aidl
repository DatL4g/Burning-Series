package dev.datlag.mimasu.core.update;

interface IUpdateInfo {
    boolean available();

    boolean required();

    @nullable String playstore();

    @nullable String directDownload();
}