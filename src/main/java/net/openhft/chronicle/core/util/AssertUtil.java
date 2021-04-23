package net.openhft.chronicle.core.util;

public final class AssertUtil {

    /**
     * Setting this variable to false will disable
     * assertions in this package and will almost certainly
     * remove the assertion byte code from the target
     * jar(s).
     *
     *
     *
     */
    public static final boolean SKIP_ASSERTIONS = false;

    // Suppresses default constructor, ensuring non-instantiability.
    private AssertUtil() {
    }
}
