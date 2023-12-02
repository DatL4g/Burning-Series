package browser.tabs

/**
 * The tab's loading status.
 */
public enum class TabStatus(
    private val `value`: String,
) {
    unloaded("unloaded"),
    loading("loading"),
    complete("complete"),
    ;

    public override fun toString(): String = value
}