package dev.datlag.mimasu.core.update;

import dev.datlag.mimasu.core.update.IUpdateInfo;

interface IUpdateCheckCallback {
    void onUpdateInfo(in IUpdateInfo updateInfo);
}