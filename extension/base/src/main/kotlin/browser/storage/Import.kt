@file:JsModule("webextension-polyfill")
@file:JsQualifier("storage")

package browser.storage

import browser.events.Event

/**
 * Items in the <code>sync</code> storage area are synced using Chrome Sync.
 */
public external val sync: StorageArea = definedExternally

/**
 * Fired when one or more items change.
 */
public external val onChanged: Event<OnChangedListener, Unit> = definedExternally