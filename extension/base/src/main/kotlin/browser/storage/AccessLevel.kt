package browser.storage

import kotlin.String

/**
 * The storage area's access level.
 */
public enum class AccessLevel(
    private val `value`: String,
) {
    TRUSTED_CONTEXTS("TRUSTED_CONTEXTS"),
    TRUSTED_AND_UNTRUSTED_CONTEXTS("TRUSTED_AND_UNTRUSTED_CONTEXTS"),
    ;

    public override fun toString(): String = value
}