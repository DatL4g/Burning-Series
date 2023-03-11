@file:JsModule("webextension-polyfill")
@file:JsQualifier("runtime")

package browser.runtime

import browser.events.Event
import kotlin.js.Promise

/**
 * Sends a single message to event listeners within your extension/app or a different extension/app.
 * Similar to $(ref:runtime.connect) but only sends a single message, with an optional response. If
 * sending to your extension, the $(ref:runtime.onMessage) event will be fired in every frame of your
 * extension (except for the sender's frame), or $(ref:runtime.onMessageExternal), if a different
 * extension. Note that extensions cannot send messages to content scripts using this method. To send
 * messages to content scripts, use $(ref:tabs.sendMessage).
 *
 * @param extensionId The ID of the extension/app to send the message to. If omitted, the message
 * will be sent to your own extension/app. Required if sending messages from a web page for <a
 * href="manifest/externally_connectable.html">web messaging</a>.
 * @param message The message to send. This message should be a JSON-ifiable object.
 */
public external fun sendMessage(
    extensionId: String? = definedExternally,
    message: Any,
    options: SendMessageOptions? = definedExternally
): Promise<Any>?

/**
 * Fired when a message is sent from either an extension process (by $(ref:runtime.sendMessage)) or
 * a content script (by $(ref:tabs.sendMessage)).
 */
public external val onMessage: Event<OnMessageListener, Any?> = definedExternally