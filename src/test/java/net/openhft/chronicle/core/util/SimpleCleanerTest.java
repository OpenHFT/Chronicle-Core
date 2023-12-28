package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.util.SimpleCleaner;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

class SimpleCleanerTest {

    @Test
    void cleanShouldExecuteRunnableOnce() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        cleaner.clean();
        cleaner.clean(); // Second call to check idempotency

        verify(runnable, times(1)).run();
    }

    @Test
    void cleanShouldNotExecuteRunnableIfAlreadyCleaned() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        cleaner.clean(); // First call
        cleaner.clean(); // Second call

        verify(runnable, times(1)).run();
    }

    @Test
    void constructorShouldInitializeWithProvidedRunnable() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        assertNotNull(cleaner); // Verifying that cleaner is initialized
        // Further tests can be performed if needed to check internal state
    }
}
