package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rob Austin.
 */
public class Timer {

    static final Logger LOG = LoggerFactory.getLogger(Timer.class);
    private final EventLoop eventLoop;

    /**
     * @param eventLoop the event loop that the timer task is run on
     */
    public Timer(@NotNull EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    /**
     * uses the event loop thread to call the event handler periodically, the time that the event is
     * called back is best efforts, but if the thread is busy that call back maybe delayed
     *
     * @param eventHandler   the handler to be called back
     * @param initialDelayMs how log in milliseconds to wait before being called back
     * @param periodMs       the poll interval of being called
     */
    public void scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                    long initialDelayMs,
                                    long periodMs) {
        eventLoop.addHandler(new ScheduledEventHandler(eventHandler, initialDelayMs, periodMs));
    }

    public void schedule(VanillaEventHandler eventHandler, long periodMs) {
        eventLoop.addHandler(new ScheduledEventHandler(eventHandler, 0, periodMs));
    }

    private class ScheduledEventHandler implements EventHandler {

        private final VanillaEventHandler eventHandler;
        private final long initialDelayMs;
        private final long periodMs;

        private boolean isFirstTime = true;
        private long lastTimeRan = System.currentTimeMillis();

        private ScheduledEventHandler(@NotNull VanillaEventHandler eventHandler,
                                      long initialDelayMs,
                                      long periodMs) {
            this.initialDelayMs = initialDelayMs;
            this.periodMs = periodMs;
            this.eventHandler = eventHandler;
        }

        @Override
        public boolean action() throws InvalidEventHandlerException, InterruptedException {

            long currentTime = System.currentTimeMillis();

            if (lastTimeRan + waitTimeMs() > currentTime)
                return false;

            lastTimeRan = currentTime;

            try {
                return eventHandler.action();
            } catch (RuntimeException e) {
                Jvm.warn().on(getClass(), "Unexpected runtime exception", e);
            }

            return false;
        }

        private long waitTimeMs() {
            if (!isFirstTime)
                return periodMs;

            isFirstTime = false;
            return initialDelayMs;
        }

        @Override
        @NotNull
        public HandlerPriority priority() {
            return HandlerPriority.TIMER;
        }
    }
}

