package net.openhft.chronicle.core.internal.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NopThreadConfinementAsserterTest {

    @Test
    public void assertThreadConfinedShouldDoNothing() {
        NopThreadConfinementAsserter asserter = NopThreadConfinementAsserter.INSTANCE;

        assertDoesNotThrow(asserter::assertThreadConfined, "assertThreadConfined should not throw any exceptions");
    }
}
