@file:JsModule("webextension-polyfill")
@file:JsQualifier("tabs")

package browser.tabs

public external interface Tab {
    /**
     * The ID of the tab. Tab IDs are unique within a browser session. Under some circumstances a tab
     * may not be assigned an ID; for example, when querying foreign tabs using the $(ref:sessions) API,
     * in which case a session ID may be present. Tab ID can also be set to
     * <code>chrome.tabs.TAB_ID_NONE</code> for apps and devtools windows.
     */
    public var id: Int?

    /**
     * The zero-based index of the tab within its window.
     */
    public var index: Int

    /**
     * The ID of the group that the tab belongs to.
     */
    public var groupId: Int

    /**
     * The ID of the window that contains the tab.
     */
    public var windowId: Int

    /**
     * The ID of the tab that opened this tab, if any. This property is only present if the opener tab
     * still exists.
     */
    public var openerTabId: Int?

    /**
     * Whether the tab is selected.
     */
    @Deprecated(
        message = "Please use ${'$'}(ref:tabs.Tab.highlighted).",
        level = DeprecationLevel.WARNING,
    )
    public var selected: Boolean

    /**
     * Whether the tab is highlighted.
     */
    public var highlighted: Boolean

    /**
     * Whether the tab is active in its window. Does not necessarily mean the window is focused.
     */
    public var active: Boolean

    /**
     * Whether the tab is pinned.
     */
    public var pinned: Boolean

    /**
     * Whether the tab has produced sound over the past couple of seconds (but it might not be heard
     * if also muted). Equivalent to whether the 'speaker audio' indicator is showing.
     */
    public var audible: Boolean?

    /**
     * Whether the tab is discarded. A discarded tab is one whose content has been unloaded from
     * memory, but is still visible in the tab strip. Its content is reloaded the next time it is
     * activated.
     */
    public var discarded: Boolean

    /**
     * Whether the tab can be discarded automatically by the browser when resources are low.
     */
    public var autoDiscardable: Boolean

    /**
     * The tab's muted state and the reason for the last state change.
     */
    public var mutedInfo: MutedInfo?

    /**
     * The last committed URL of the main frame of the tab. This property is only present if the
     * extension's manifest includes the <code>"tabs"</code> permission and may be an empty string if the
     * tab has not yet committed. See also $(ref:Tab.pendingUrl).
     */
    public var url: String?

    /**
     * The URL the tab is navigating to, before it has committed. This property is only present if the
     * extension's manifest includes the <code>"tabs"</code> permission and there is a pending
     * navigation.
     */
    public var pendingUrl: String?

    /**
     * The title of the tab. This property is only present if the extension's manifest includes the
     * <code>"tabs"</code> permission.
     */
    public var title: String?

    /**
     * The URL of the tab's favicon. This property is only present if the extension's manifest
     * includes the <code>"tabs"</code> permission. It may also be an empty string if the tab is loading.
     */
    public var favIconUrl: String?

    /**
     * The tab's loading status.
     */
    public var status: TabStatus?

    /**
     * Whether the tab is in an incognito window.
     */
    public var incognito: Boolean

    /**
     * The width of the tab in pixels.
     */
    public var width: Int?

    /**
     * The height of the tab in pixels.
     */
    public var height: Int?

    /**
     * The session ID used to uniquely identify a tab obtained from the $(ref:sessions) API.
     */
    public var sessionId: String?
}