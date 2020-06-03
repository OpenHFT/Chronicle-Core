package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.AbstractCloseable;
import org.junit.After;
import org.junit.Before;

public class CoreTestCommon {

    @Before
    public void enableCloseableTracing() {
        AbstractCloseable.enableCloseableTracing();
    }

    @After
    public void assertCloseablesClosed() {
        AbstractCloseable.assertCloseablesClosed();
    }
}
