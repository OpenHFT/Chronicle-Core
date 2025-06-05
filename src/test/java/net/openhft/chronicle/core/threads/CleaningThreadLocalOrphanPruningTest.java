package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.util.ThrowingConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit-tests the constructor flag {@code overrideTrackNonCleaningThreads}.
 *
 * <p>The same test body is run twice by JUnit's parameter-mechanism:
 * once with tracking <em>forced ON</em>, once with tracking <em>forced OFF</em>.
 * A helper spins 16 short-lived ordinary threads to produce "orphan" values,
 * then waits for all of them to finish.</p>
 */
class CleaningThreadLocalOrphanPruningTest {

    private static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(Boolean.TRUE, true),
                Arguments.of(Boolean.FALSE, false)
        );
    }

    @ParameterizedTest(name = "track={0} -> expectCleanup={1}")
    @MethodSource("cases")
    void orphanPruningBehaviour(Boolean trackFlag, boolean expectCleanup) throws InterruptedException {

        /* 1. flag toggled by cleanup lambda */
        AtomicInteger cleaned = new AtomicInteger();
        ThrowingConsumer<AtomicBoolean, Exception> cleaner = a -> cleaned.incrementAndGet();

        /* 2. build CTL with explicit tracking flag */
        Supplier<AtomicBoolean> supplier = AtomicBoolean::new;
        CleaningThreadLocal<AtomicBoolean> ctl =
                new CleaningThreadLocal<>(supplier, cleaner,
                        UnaryOperator.identity(), trackFlag);

        /* 3. produce orphans on 128 threads */
        int threads = 128;
        forkAndJoin(threads, () -> ctl.set(new AtomicBoolean()));

        /* 4. sweep */
        CleaningThreadLocal.cleanupNonCleaningThreads();

        /* 5. assert */
        if (expectCleanup)
            assertEquals(threads, cleaned.get(), "cleanup should have run");
        else
            assertEquals(0, cleaned.get(), "cleanup should NOT have run");
    }

    /**
     * Runs {@code job} concurrently on {@code n} short-lived threads and waits for all to die.
     */
    private static void forkAndJoin(int n, Runnable job) throws InterruptedException {
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++)
            threads[i] = new Thread(job, "orphan-producer-" + i);

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}
