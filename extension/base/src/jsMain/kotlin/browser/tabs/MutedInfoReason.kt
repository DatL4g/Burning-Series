package browser.tabs

/**
 * An event that caused a muted state change.
 */
public enum class MutedInfoReason(
    private val `value`: String,
) {
    /**
     * A user input action set the muted state.
     */
    user("user"),
    /**
     * Tab capture was started, forcing a muted state change.
     */
    capture("capture"),
    /**
     * An extension, identified by the extensionId field, set the muted state.
     */
    extension("extension"),
    ;

    public override fun toString(): String = value
}