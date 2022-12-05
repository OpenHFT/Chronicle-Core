package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.time.SetTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimerTest {

    public static final int INITIAL_DELAY_MS = 1_000;
    public static final int PERIOD_MS = 2_000;
    @Mock
    private EventLoop eventLoop;
    @Mock
    private VanillaEventHandler handler;
    @Mock
    private Runnable runnable;

    private Timer.ScheduledEventHandler scheduledEventHandler;
    private Timer timer;
    private SetTimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        timeProvider = new SetTimeProvider();
        doAnswer(iom -> {
            scheduledEventHandler = iom.getArgument(0);
            return null;
        }).when(eventLoop).addHandler(any(EventHandler.class));
        timer = new Timer(eventLoop, timeProvider);
    }

    @Test
    void willExecuteScheduledTaskPeriodically() throws InvalidEventHandlerException {
        final long submittedTime = System.currentTimeMillis();
        timeProvider.currentTimeMillis(submittedTime);
        timer.scheduleAtFixedRate(handler, INITIAL_DELAY_MS, PERIOD_MS);

        // Handler is not called before initialDelayMs
        scheduledEventHandler.action();
        verifyNoInteractions(handler);

        // Handler is called after initialDelayMs
        final long firstCallTime = submittedTime + INITIAL_DELAY_MS + 1;
        timeProvider.currentTimeMillis(firstCallTime);
        scheduledEventHandler.action();
        verify(handler).action();
        reset(handler);

        // Handler is not called again before periodMs
        timeProvider.currentTimeMillis(firstCallTime + PERIOD_MS - 10);
        scheduledEventHandler.action();
        verifyNoInteractions(handler);

        // Handler is called again after periodMs
        timeProvider.currentTimeMillis(firstCallTime + PERIOD_MS + 10);
        scheduledEventHandler.action();
        verify(handler).action();
    }

    @Test
    void willSubmitHandlerWithConfiguredPriority() {
        final HandlerPriority configuredPriority = HandlerPriority.REPLICATION_TIMER;
        timer.scheduleAtFixedRate(handler, INITIAL_DELAY_MS, PERIOD_MS, configuredPriority);
        assertEquals(configuredPriority, scheduledEventHandler.priority());
    }

    @Test
    void willSubmitHandlerWithTimerPriorityByDefault() {
        timer.scheduleAtFixedRate(handler, INITIAL_DELAY_MS, PERIOD_MS);
        assertEquals(HandlerPriority.TIMER, scheduledEventHandler.priority());
    }

    @Test
    void willThrowInvalidEventHandlerWhenCloseIsCalled() throws InvalidEventHandlerException, IOException {
        final Closeable closeable = timer.scheduleAtFixedRate(handler, INITIAL_DELAY_MS, PERIOD_MS);

        // This should not throw
        scheduledEventHandler.action();

        closeable.close();
        assertThrows(InvalidEventHandlerException.class, () -> scheduledEventHandler.action());
    }

    @Test
    void willScheduleSingleExecutionTask() throws InvalidEventHandlerException {
        final long submittedTime = System.currentTimeMillis();
        timeProvider.currentTimeMillis(submittedTime);
        timer.schedule(runnable, INITIAL_DELAY_MS);

        // Handler is not called before initialDelayMs
        scheduledEventHandler.action();
        verifyNoInteractions(handler);

        // Handler is called after initialDelayMs and InvalidEventHandlerExceptionIsThrown
        final long firstCallTime = submittedTime + INITIAL_DELAY_MS + 1;
        timeProvider.currentTimeMillis(firstCallTime);
        assertThrows(InvalidEventHandlerException.class, () -> scheduledEventHandler.action());
        verify(runnable).run();
    }

    @Test
    void canCancelSingleExecutionTask() throws InvalidEventHandlerException, IOException {
        final long submittedTime = System.currentTimeMillis();
        timeProvider.currentTimeMillis(submittedTime);
        final Closeable closeable = timer.schedule(runnable, INITIAL_DELAY_MS);

        // Handler is not called before initialDelayMs
        scheduledEventHandler.action();
        verifyNoInteractions(handler);

        closeable.close();

        // Handler is NOT called after initialDelayMs because it was cancelled, but InvalidEventHandlerExceptionIsThrown
        final long firstCallTime = submittedTime + INITIAL_DELAY_MS + 1;
        timeProvider.currentTimeMillis(firstCallTime);
        assertThrows(InvalidEventHandlerException.class, () -> scheduledEventHandler.action());
        verifyNoInteractions(runnable);
    }
}