// IUpdateService.aidl
package dev.datlag.mimasu.core.update;

import dev.datlag.mimasu.core.update.IUpdateCheckCallback;

interface IUpdateService {
    void hasUpdate(in IUpdateCheckCallback callback);
}