@file:JsModule("webextension-polyfill")
@file:JsQualifier("runtime")

package browser.runtime

import browser.tabs.Tab

/**
 * An object containing information about the script context that sent a message or request.
 */
public external interface MessageSender {
    /**
     * The $(ref:tabs.Tab) which opened the connection, if any. This property will
     * <strong>only</strong> be present when the connection was opened from a tab (including content
     * scripts), and <strong>only</strong> if the receiver is an extension, not an app.
     */
    public var tab: Tab?

    /**
     * The <a href='webNavigation#frame_ids'>frame</a> that opened the connection. 0 for top-level
     * frames, positive for child frames. This will only be set when <code>tab</code> is set.
     */
    public var frameId: Int?

    /**
     * The guest process id of the requesting webview, if available. Only available for component
     * extensions.
     */
    public var guestProcessId: Int?

    /**
     * The guest render frame routing id of the requesting webview, if available. Only available for
     * component extensions.
     */
    public var guestRenderFrameRoutingId: Int?

    /**
     * The ID of the extension or app that opened the connection, if any.
     */
    public var id: String?

    /**
     * The URL of the page or frame that opened the connection. If the sender is in an iframe, it will
     * be iframe's URL not the URL of the page which hosts it.
     */
    public var url: String?

    /**
     * The name of the native application that opened the connection, if any.
     */
    public var nativeApplication: String?

    /**
     * The TLS channel ID of the page or frame that opened the connection, if requested by the
     * extension or app, and if available.
     */
    public var tlsChannelId: String?

    /**
     * The origin of the page or frame that opened the connection. It can vary from the url property
     * (e.g., about:blank) or can be opaque (e.g., sandboxed iframes). This is useful for identifying if
     * the origin can be trusted if we can't immediately tell from the URL.
     */
    public var origin: String?

    /**
     * A UUID of the document that opened the connection.
     */
    public var documentId: String?

    /**
     * The lifecycle the document that opened the connection is in at the time the port was created.
     * Note that the lifecycle state of the document may have changed since port creation.
     */
    public var documentLifecycle: String?
}