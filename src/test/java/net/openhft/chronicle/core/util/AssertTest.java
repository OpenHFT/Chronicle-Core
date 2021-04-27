package net.openhft.chronicle.core.util;

import static net.openhft.chronicle.core.util.AssertUtil.SKIP_ASSERTIONS;
import static net.openhft.chronicle.core.util.Ints.*;

public final class AssertTest {

    public static void testWithAssertDirectly() {
        assert SKIP_ASSERTIONS || negative().negate().test(3);
    }

    public static void testWithAssertDirectlyWithText() {
        assert SKIP_ASSERTIONS || negative().negate().test(3) : failDescription(negative().negate(), 3);
    }

    public static void testWithAssertMethod() {
        assertIfEnabled(negative().negate(), 3);
    }

}
