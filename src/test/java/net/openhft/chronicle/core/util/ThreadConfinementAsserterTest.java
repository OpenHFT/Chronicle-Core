package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ThreadConfinementAsserterTest {

    @Test
    void assertThreadConfinedSameThread() {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();

        assertDoesNotThrow(asserter::assertThreadConfined);
        assertDoesNotThrow(asserter::assertThreadConfined);
    }

    @Test
    void assertThreadConfinedDifferentThreads() throws InterruptedException {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        assertDoesNotThrow(asserter::assertThreadConfined);

        executorService.execute(() -> {
            assertThrows(IllegalStateException.class, asserter::assertThreadConfined);
            latch.countDown();
        });

        latch.await();
        executorService.shutdown();
    }

    @Test
    void createShouldReturnCorrectTypeBasedOnAssertions() {
        // This test's behavior will depend on whether assertions are enabled in the JVM.
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.create();
        // Perform tests based on whether assertions are enabled or not.
    }

    @Test
    void createEnabledShouldAlwaysReturnFunctionalAsserter() {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();
        assertNotNull(asserter);
        // Further testing of functionality.
    }
}
