package net.openhft.chronicle.core.invariant;

public final class AssertUtil {

    /**
     * Setting this variable to false will disable
     * assertions in this package and will likely
     * remove the assertion byte code from the target
     * jar(s).
     */
    static final boolean USE_ASSERTIONS = true;

    // Suppresses default constructor, ensuring non-instantiability.
    private AssertUtil() {
    }
}
