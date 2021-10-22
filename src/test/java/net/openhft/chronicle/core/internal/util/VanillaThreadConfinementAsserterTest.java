package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class VanillaThreadConfinementAsserterTest {

    private ThreadConfinementAsserter asserter;

    @Before
    public void before() {
        asserter = new VanillaThreadConfinementAsserter();
    }

    @Test
    public void assertThreadConfinedSame() {
        asserter.assertThreadConfined();
        asserter.createIfAssertionsEnabled();
    }

    @Test
    public void assertThreadConfinedOther() throws InterruptedException {
        final Thread other = new Thread(() -> asserter.assertThreadConfined(), "first");
        other.start();
        other.join();

        // The asserter is now touched by another thread
        assertThrows(IllegalStateException.class, () ->
                asserter.assertThreadConfined()
        );
    }

}