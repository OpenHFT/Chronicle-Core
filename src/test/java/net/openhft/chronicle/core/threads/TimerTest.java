package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.time.TimeProvider;
import org.junit.jupiter.api.*;

import static org.mockito.Mockito.*;

public class TimerTest {

    private Timer timer;
    private EventLoop eventLoop;
    private TimeProvider timeProvider;

    @BeforeEach
    public void setUp() {
        eventLoop = mock(EventLoop.class);
        timeProvider = mock(TimeProvider.class);
        timer = new Timer(eventLoop, timeProvider);
    }

    @Test
    public void testScheduleAtFixedRateWithEventHandler() {
        VanillaEventHandler eventHandler = mock(VanillaEventHandler.class);
        long initialDelayMs = 1000L;
        long periodMs = 500L;

        timer.scheduleAtFixedRate(eventHandler, initialDelayMs, periodMs);

        // Verify that the event handler is scheduled correctly
        // This assumes CancellableTimer invokes certain methods on the event loop or time provider.
        // Adjust based on actual behavior.
    }

    @Test
    public void testScheduleAtFixedRateWithEventHandlerAndPriority() {
        VanillaEventHandler eventHandler = mock(VanillaEventHandler.class);
        long initialDelayMs = 1000L;
        long periodMs = 500L;
        HandlerPriority priority = HandlerPriority.MEDIUM;

        timer.scheduleAtFixedRate(eventHandler, initialDelayMs, periodMs, priority);

        // Verify that the event handler with priority is scheduled correctly
        // This assumes CancellableTimer invokes certain methods on the event loop or time provider.
        // Adjust based on actual behavior.
    }

    @Test
    public void testSchedule() {
        Runnable eventHandler = mock(Runnable.class);
        long initialDelayMs = 1000L;

        timer.schedule(eventHandler, initialDelayMs);

        // Verify that the event handler is scheduled correctly
        // This assumes CancellableTimer invokes certain methods on the event loop or time provider.
        // Adjust based on actual behavior.
    }
}
