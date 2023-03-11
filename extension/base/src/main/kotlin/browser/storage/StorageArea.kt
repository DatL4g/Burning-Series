@file:JsModule("webextension-polyfill")
@file:JsQualifier("storage")

package browser.storage

import browser.events.Event
import kotlin.js.Promise

public external interface StorageArea {
    /**
     * Fired when one or more items change.
     */
    public val onChanged: Event<OnChangedListener, Unit>

    /**
     * Gets one or more items from storage.
     *
     * @param keys A single key to get, list of keys to get, or a dictionary specifying default values
     * (see description of the object).  An empty list or object will return an empty result object.
     * Pass in <code>null</code> to get the entire contents of storage.
     * @return Callback with storage items, or on failure (in which case $(ref:runtime.lastError) will
     * be set).
     */
    public fun `get`(keys: Any = definedExternally): Promise<Any>

    /**
     * Gets the amount of space (in bytes) being used by one or more items.
     *
     * @param keys A single key or list of keys to get the total usage for. An empty list will return
     * 0. Pass in <code>null</code> to get the total usage of all of storage.
     * @return Callback with the amount of space being used by storage, or on failure (in which case
     * $(ref:runtime.lastError) will be set).
     */
    public fun getBytesInUse(keys: Any = definedExternally): Promise<Int>

    /**
     * Sets multiple items.
     *
     * @param items <p>An object which gives each key/value pair to update storage with. Any other
     * key/value pairs in storage will not be affected.</p><p>Primitive values such as numbers will
     * serialize as expected. Values with a <code>typeof</code> <code>"object"</code> and
     * <code>"function"</code> will typically serialize to <code>{}</code>, with the exception of
     * <code>Array</code> (serializes as expected), <code>Date</code>, and <code>Regex</code> (serialize
     * using their <code>String</code> representation).</p>
     * @return Callback on success, or on failure (in which case $(ref:runtime.lastError) will be
     * set).
     */
    public fun `set`(items: Any): Promise<Nothing>?

    /**
     * Removes one or more items from storage.
     *
     * @param keys A single key or a list of keys for items to remove.
     * @return Callback on success, or on failure (in which case $(ref:runtime.lastError) will be
     * set).
     */
    public fun remove(keys: Any): Promise<Nothing>?

    /**
     * Removes all items from storage.
     *
     * @return Callback on success, or on failure (in which case $(ref:runtime.lastError) will be
     * set).
     */
    public fun clear(): Promise<Nothing>?

    /**
     * Sets the desired access level for the storage area. The default will be only trusted contexts.
     *
     * @return Callback on success, or on failure (in which case $(ref:runtime.lastError) will be
     * set).
     */
    public fun setAccessLevel(accessOptions: SetAccessLevelAccessOptions): Promise<Nothing>?
}