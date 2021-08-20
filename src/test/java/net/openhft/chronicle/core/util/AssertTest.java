package net.openhft.chronicle.core.util;

import static net.openhft.chronicle.core.util.AssertUtil.SKIP_ASSERTIONS;
import static net.openhft.chronicle.core.util.Ints.assertIfEnabled;
import static net.openhft.chronicle.core.util.Ints.nonNegative;

public final class AssertTest {

    public static void testWithAssertDirectly(int x) {
        assert SKIP_ASSERTIONS || x >= 0 ;
    }

    public static void testWithAssertDirectlyWithText(int x) {
        assert SKIP_ASSERTIONS || assertIfEnabled(nonNegative(), x);
    }

    public static void testWithAssertMethod(int x) {
        assertIfEnabled(nonNegative(), x);
    }
}
