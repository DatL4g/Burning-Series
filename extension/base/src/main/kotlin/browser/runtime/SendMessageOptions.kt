@file:JsModule("webextension-polyfill")
@file:JsQualifier("runtime")

package browser.runtime

external interface SendMessageOptions {

    /**
     * Whether the TLS channel ID will be passed into onMessageExternal for processes that are
     * listening for the connection event.
     */
    public var includeTlsChannelId: Boolean?
}