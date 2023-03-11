@file:JsModule("webextension-polyfill")
@file:JsQualifier("storage")

package browser.storage

/**
 * Fired when one or more items change.
 */
public external interface OnChangedListener {
    /**
     * Object mapping each key that changed to its corresponding $(ref:storage.StorageChange) for that
     * item.
     */
    public var changes: Any

    /**
     * The name of the storage area (<code>"sync"</code>, <code>"local"</code> or
     * <code>"managed"</code>) the changes are for.
     */
    public var areaName: String
}