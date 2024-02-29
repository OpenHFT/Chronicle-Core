package net.openhft.chronicle.core;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ChronicleInitRunnableTest {

    @Test
    public void testRunMethod() {
        ChronicleInitRunnable runnable = mock(ChronicleInitRunnable.class);
        runnable.run();

        // Verify that the run method was called.
        verify(runnable, times(1)).run();
    }

    @Test
    public void testPostInitDefaultMethod() {
        ChronicleInitRunnable runnable = new ChronicleInitRunnable() {
            @Override
            public void run() {

            }
            // No override, use default implementation.
        };

        // Call postInit and verify that it does not throw an exception.
        // Since it's a no-op by default, there's no direct result to assert.
        assertDoesNotThrow(runnable::postInit);
    }

    @Test
    public void testOverriddenPostInitMethod() {
        ChronicleInitRunnable runnable = new ChronicleInitRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void postInit() {
                // Custom implementation for testing.
            }
        };

        // Verify that the custom postInit does not throw an exception.
        // This is just to confirm that the method can be successfully overridden.
        assertDoesNotThrow(runnable::postInit);
    }
}
