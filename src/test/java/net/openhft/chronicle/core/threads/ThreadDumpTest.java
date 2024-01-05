package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.*;

import static org.junit.Assume.assumeFalse;
import static org.junit.jupiter.api.Assertions.*;

public class ThreadDumpTest {

    private ThreadDump threadDump;

    @BeforeEach
    void setUp() {
        threadDump = new ThreadDump();
    }

    @Test
    public void testIgnoreThread() {
        String ignoredThreadName = "IgnoredThread";
        threadDump.ignore(ignoredThreadName);

        // Simulate an ignored thread
        Thread ignoredThread = new Thread(() -> {}, ignoredThreadName);
        ignoredThread.start();

        threadDump.assertNoNewThreads();

        // Clean up
        ignoredThread.interrupt();
    }

    @Test
    void testAssertNoNewThreads() {
        threadDump.assertNoNewThreads();
    }

    @Test
    void testAssertNewThreads() {
        assumeFalse(Jvm.isArm());
        Thread newThread = new Thread(() -> {
            Jvm.pause(1000);
        });
        newThread.start();

        // Expect an AssertionError since a new thread is running
        assertThrows(AssertionError.class, threadDump::assertNoNewThreads);

        // Clean up
        newThread.interrupt();
    }
}
