@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package browser.storage

public inline fun SetAccessLevelAccessOptions(block: SetAccessLevelAccessOptions.() -> Unit):
        SetAccessLevelAccessOptions = (js("{}") as SetAccessLevelAccessOptions).apply(block)

public inline fun OnChangedListener(block: OnChangedListener.() -> Unit): OnChangedListener =
    (js("{}") as OnChangedListener).apply(block)

public inline fun StorageArea(block: StorageArea.() -> Unit): StorageArea = (js("{}") as
        StorageArea).apply(block)