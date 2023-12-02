@file:JsModule("webextension-polyfill")
@file:JsQualifier("events")

package browser.events

/**
 * An object which allows the addition and removal of listeners for a Chrome event.
 */
public external interface Event<T, R> {
    /**
     * Registers an event listener <em>callback</em> to an event.
     *
     * @param callback Called when an event occurs. The parameters of this function depend on the type
     * of event.
     */
    public fun addListener(callback: (T) -> R): Unit

    /**
     * Registers an event listener <em>callback</em> to an event.
     *
     * @param callback Called when an event occurs. The parameters of this function depend on the type
     * of event.
     */
    public fun addListener(callback: (T) -> R, vararg params: Any?): Unit

    /**
     * Deregisters an event listener <em>callback</em> from an event.
     *
     * @param callback Listener that shall be unregistered.
     */
    public fun removeListener(callback: (T) -> R): Unit

    /**
     * @param callback Listener whose registration status shall be tested.
     * @return True if <em>callback</em> is registered to the event.
     */
    public fun hasListener(callback: (T) -> R): Boolean

    /**
     * @return True if any event listeners are registered to the event.
     */
    public fun hasListeners(): Boolean

    /**
     * Registers rules to handle events.
     *
     * @param eventName Name of the event this function affects.
     * @param webViewInstanceId If provided, this is an integer that uniquely identfies the <webview>
     * associated with this function call.
     * @param rules Rules to be registered. These do not replace previously registered rules.
     * @param callback Rules that were registered, the optional parameters are filled with values.
     */
    public fun <C, A> addRules(
        eventName: String? = definedExternally,
        webViewInstanceId: Int? = definedExternally,
        rules: Array<Rule<C, A>>,
        callback: ((Array<Rule<C, A>>) -> Unit)? = definedExternally,
    ): Unit

    /**
     * Registers rules to handle events.
     *
     * @param rules Rules to be registered. These do not replace previously registered rules.
     * @param callback Rules that were registered, the optional parameters are filled with values.
     */
    public fun <C, A> addRules(rules: Array<Rule<C, A>>, callback: ((Array<Rule<C, A>>) -> Unit)? =
        definedExternally): Unit

    /**
     * Returns currently registered rules.
     *
     * @param eventName Name of the event this function affects.
     * @param webViewInstanceId If provided, this is an integer that uniquely identfies the <webview>
     * associated with this function call.
     * @param ruleIdentifiers If an array is passed, only rules with identifiers contained in this
     * array are returned.
     * @param callback Rules that were registered, the optional parameters are filled with values.
     */
    public fun <C, A> getRules(
        eventName: String? = definedExternally,
        webViewInstanceId: Int? = definedExternally,
        ruleIdentifiers: Array<String>? = definedExternally,
        callback: (Array<Rule<C, A>>) -> Unit,
    ): Unit

    /**
     * Returns currently registered rules.
     *
     * @param ruleIdentifiers If an array is passed, only rules with identifiers contained in this
     * array are returned.
     * @param callback Rules that were registered, the optional parameters are filled with values.
     */
    public fun <C, A> getRules(ruleIdentifiers: Array<String>? = definedExternally,
                               callback: (Array<Rule<C, A>>) -> Unit): Unit

    /**
     * Unregisters currently registered rules.
     *
     * @param eventName Name of the event this function affects.
     * @param webViewInstanceId If provided, this is an integer that uniquely identfies the <webview>
     * associated with this function call.
     * @param ruleIdentifiers If an array is passed, only rules with identifiers contained in this
     * array are returned.
     * @param callback Rules that were registered, the optional parameters are filled with values.
     */
    public fun <C, A> removeRules(
        eventName: String? = definedExternally,
        webViewInstanceId: Int? = definedExternally,
        ruleIdentifiers: Array<String>? = definedExternally,
        callback: ((Array<Rule<C, A>>) -> Unit)? = definedExternally,
    ): Unit

    /**
     * Unregisters currently registered rules.
     *
     * @param ruleIdentifiers If an array is passed, only rules with identifiers contained in this
     * array are returned.
     * @param callback Rules that were registered, the optional parameters are filled with values.
     */
    public fun <C, A> removeRules(ruleIdentifiers: Array<String>? = definedExternally,
                                  callback: ((Array<Rule<C, A>>) -> Unit)? = definedExternally): Unit
}