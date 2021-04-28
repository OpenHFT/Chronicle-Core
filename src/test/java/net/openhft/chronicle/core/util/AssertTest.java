package net.openhft.chronicle.core.util;

import static net.openhft.chronicle.core.util.AssertUtil.SKIP_ASSERTIONS;
import static net.openhft.chronicle.core.util.Ints.*;

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
/*
    @Test
    public void a() {
        setInt(new byte[10], 8, 13);
    }

    public void setInt(byte[] bytes, int offset, int value) {
        assertIfEnabled(Ints.betweenZeroAndReserving(),offset, bytes.length, Integer.BYTES);
        // set value via unsafe
    }*/

}
