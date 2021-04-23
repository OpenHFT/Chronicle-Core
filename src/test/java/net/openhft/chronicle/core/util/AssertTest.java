package net.openhft.chronicle.core.util;

import static net.openhft.chronicle.core.util.AssertUtil.SKIP_ASSERTIONS;
import static net.openhft.chronicle.core.util.IntCondition.NON_NEGATIVE;
import static net.openhft.chronicle.core.util.Ints.*;

public final class AssertTest {

    public static void testWithAssertDirectly() {
        assert SKIP_ASSERTIONS || NON_NEGATIVE.test(3);
    }

    public static void testWithAssertDirectlyWithText() {
        assert SKIP_ASSERTIONS || NON_NEGATIVE.test(3) : failDescription(NON_NEGATIVE, 3);
    }

    public static void testWithAssertMethod() {
        assertIfEnabled(NON_NEGATIVE, 3);
    }

}
