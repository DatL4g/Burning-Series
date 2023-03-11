@file:JsModule("webextension-polyfill")
@file:JsQualifier("storage")

package browser.storage

public external interface SetAccessLevelAccessOptions {
    /**
     * The access level of the storage area.
     */
    public var accessLevel: AccessLevel
}