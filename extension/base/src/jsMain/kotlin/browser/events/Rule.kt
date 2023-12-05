@file:JsModule("webextension-polyfill")
@file:JsQualifier("events")

package browser.events

/**
 * Description of a declarative rule for handling events.
 */
public external interface Rule<C, A> {
    /**
     * Optional identifier that allows referencing this rule.
     */
    public var id: String?

    /**
     * Tags can be used to annotate rules and perform operations on sets of rules.
     */
    public var tags: Array<String>?

    /**
     * List of conditions that can trigger the actions.
     */
    public var conditions: Array<C>

    /**
     * List of actions that are triggered if one of the conditions is fulfilled.
     */
    public var actions: Array<A>

    /**
     * Optional identifier that allows referencing this rule.
     */
    public var priority: Int?
}