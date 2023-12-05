@file:JsModule("webextension-polyfill")
@file:JsQualifier("events")

package browser.events

/**
 * Filters URLs for various criteria. See <a href='events#filtered'>event filtering</a>. All
 * criteria are case sensitive.
 */
public external interface UrlFilter {
    /**
     * Matches if the host name of the URL contains a specified string. To test whether a host name
     * component has a prefix 'foo', use hostContains: '.foo'. This matches 'www.foobar.com' and
     * 'foo.com', because an implicit dot is added at the beginning of the host name. Similarly,
     * hostContains can be used to match against component suffix ('foo.') and to exactly match against
     * components ('.foo.'). Suffix- and exact-matching for the last components need to be done
     * separately using hostSuffix, because no implicit dot is added at the end of the host name.
     */
    public var hostContains: String?

    /**
     * Matches if the host name of the URL is equal to a specified string.
     */
    public var hostEquals: String?

    /**
     * Matches if the host name of the URL starts with a specified string.
     */
    public var hostPrefix: String?

    /**
     * Matches if the host name of the URL ends with a specified string.
     */
    public var hostSuffix: String?

    /**
     * Matches if the path segment of the URL contains a specified string.
     */
    public var pathContains: String?

    /**
     * Matches if the path segment of the URL is equal to a specified string.
     */
    public var pathEquals: String?

    /**
     * Matches if the path segment of the URL starts with a specified string.
     */
    public var pathPrefix: String?

    /**
     * Matches if the path segment of the URL ends with a specified string.
     */
    public var pathSuffix: String?

    /**
     * Matches if the query segment of the URL contains a specified string.
     */
    public var queryContains: String?

    /**
     * Matches if the query segment of the URL is equal to a specified string.
     */
    public var queryEquals: String?

    /**
     * Matches if the query segment of the URL starts with a specified string.
     */
    public var queryPrefix: String?

    /**
     * Matches if the query segment of the URL ends with a specified string.
     */
    public var querySuffix: String?

    /**
     * Matches if the URL (without fragment identifier) contains a specified string. Port numbers are
     * stripped from the URL if they match the default port number.
     */
    public var urlContains: String?

    /**
     * Matches if the URL (without fragment identifier) is equal to a specified string. Port numbers
     * are stripped from the URL if they match the default port number.
     */
    public var urlEquals: String?

    /**
     * Matches if the URL (without fragment identifier) matches a specified regular expression. Port
     * numbers are stripped from the URL if they match the default port number. The regular expressions
     * use the <a href="https://github.com/google/re2/blob/master/doc/syntax.txt">RE2 syntax</a>.
     */
    public var urlMatches: String?

    /**
     * Matches if the URL without query segment and fragment identifier matches a specified regular
     * expression. Port numbers are stripped from the URL if they match the default port number. The
     * regular expressions use the <a href="https://github.com/google/re2/blob/master/doc/syntax.txt">RE2
     * syntax</a>.
     */
    public var originAndPathMatches: String?

    /**
     * Matches if the URL (without fragment identifier) starts with a specified string. Port numbers
     * are stripped from the URL if they match the default port number.
     */
    public var urlPrefix: String?

    /**
     * Matches if the URL (without fragment identifier) ends with a specified string. Port numbers are
     * stripped from the URL if they match the default port number.
     */
    public var urlSuffix: String?

    /**
     * Matches if the scheme of the URL is equal to any of the schemes specified in the array.
     */
    public var schemes: Array<String>?

    /**
     * Matches if the port of the URL is contained in any of the specified port lists. For example
     * <code>[80, 443, [1000, 1200]]</code> matches all requests on port 80, 443 and in the range
     * 1000-1200.
     */
    public var ports: Array<Any>?
}