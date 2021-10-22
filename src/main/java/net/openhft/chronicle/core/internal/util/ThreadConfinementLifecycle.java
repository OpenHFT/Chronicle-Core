package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ThreadConfinementLifecycle {

    private static final boolean ASSERTIONS_ENABLE = assertionsEnable();

    private ThreadConfinementLifecycle() {}

    public static ThreadConfinementAsserter create() {
        return ASSERTIONS_ENABLE
                ? new VanillaThreadConfinementAsserter()
                : NopThreadConfinementAsserter.INSTANCE;
    }

    static boolean assertionsEnable() {
        final AtomicBoolean ae = new AtomicBoolean();
        assert testAssert(ae);
        return ae.get();
    }

    private static boolean testAssert(final AtomicBoolean ae) {
        ae.set(true);
        return true;
    }

}
