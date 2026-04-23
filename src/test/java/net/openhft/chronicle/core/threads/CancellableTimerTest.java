/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.test.RecordingEventLoop;
import net.openhft.chronicle.core.test.RecordingRunnable;
import net.openhft.chronicle.core.test.RecordingVanillaEventHandler;
import net.openhft.chronicle.core.time.SetTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CancellableTimerTest extends CoreTestCommon {

    private static final int INITIAL_DELAY_MS = 1_000;
    private static final int PERIOD_MS = 2_000;
    private RecordingEventLoop eventLoop;
    private RecordingVanillaEventHandler handler;
    private RecordingRunnable runnable;

    private CancellableTimer timer;
    private SetTimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        timeProvider = new SetTimeProvider();
        eventLoop = new RecordingEventLoop();
        handler = new RecordingVanillaEventHandler();
        runnable = new RecordingRunnable();
        timer = new CancellableTimer(eventLoop, timeProvider);
    }

    @Test
    void willExecuteScheduledTaskPeriodically() throws InvalidEventHandlerException {
        final long submittedTime = System.currentTimeMillis();
        timeProvider.currentTimeMillis(submittedTime);
        timer.scheduleAtFixedRate(handler, INITIAL_DELAY_MS, PERIOD_MS);
        EventHandler scheduledEventHandler = eventLoop.lastHandler();

        // Handler is not called before initialDelayMs
        scheduledEventHandler.action();
        assertEquals(0, handler.actionCount());

        // Handler is called after initialDelayMs
        final long firstCallTime = submittedTime + INITIAL_DELAY_MS + 1;
        timeProvider.currentTimeMillis(firstCallTime);
        scheduledEventHandler.action();
        assertEquals(1, handler.actionCount());
        handler.reset();

        // Handler is not called again before periodMs
        timeProvider.currentTimeMillis(firstCallTime + PERIOD_MS - 10);
        scheduledEventHandler.action();
        assertEquals(0, handler.actionCount());

        // Handler is called again after periodMs
        timeProvider.currentTimeMillis(firstCallTime + PERIOD_MS + 10);
        scheduledEventHandler.action();
        assertEquals(1, handler.actionCount());
    }

    @Test
    void willSubmitHandlerWithConfiguredPriority() {
        final HandlerPriority configuredPriority = HandlerPriority.REPLICATION_TIMER;
        timer.scheduleAtFixedRate(handler, INITIAL_DELAY_MS, PERIOD_MS, configuredPriority);
        assertEquals(configuredPriority, eventLoop.lastHandler().priority());
    }

    @Test
    void willSubmitHandlerWithTimerPriorityByDefault() {
        timer.scheduleAtFixedRate(handler, INITIAL_DELAY_MS, PERIOD_MS);
        assertEquals(HandlerPriority.TIMER, eventLoop.lastHandler().priority());
    }

    @Test
    void willThrowInvalidEventHandlerWhenCloseIsCalled() throws InvalidEventHandlerException, IOException {
        final Closeable closeable = timer.scheduleAtFixedRate(handler, INITIAL_DELAY_MS, PERIOD_MS);
        EventHandler scheduledEventHandler = eventLoop.lastHandler();

        scheduledEventHandler.action();

        closeable.close();
        assertThrows(InvalidEventHandlerException.class, () -> scheduledEventHandler.action());
    }

    @Test
    void willScheduleSingleExecutionTask() throws InvalidEventHandlerException {
        final long submittedTime = System.currentTimeMillis();
        timeProvider.currentTimeMillis(submittedTime);
        timer.schedule(runnable, INITIAL_DELAY_MS);
        EventHandler scheduledEventHandler = eventLoop.lastHandler();

        // Handler is not called before initialDelayMs
        scheduledEventHandler.action();
        assertEquals(0, handler.actionCount());

        // Handler is called after initialDelayMs and InvalidEventHandlerExceptionIsThrown
        final long firstCallTime = submittedTime + INITIAL_DELAY_MS + 1;
        timeProvider.currentTimeMillis(firstCallTime);
        assertThrows(InvalidEventHandlerException.class, () -> scheduledEventHandler.action());
        assertEquals(1, runnable.runCount());
    }

    @Test
    void canCancelSingleExecutionTask() throws InvalidEventHandlerException, IOException {
        final long submittedTime = System.currentTimeMillis();
        timeProvider.currentTimeMillis(submittedTime);
        final Closeable closeable = timer.schedule(runnable, INITIAL_DELAY_MS);
        EventHandler scheduledEventHandler = eventLoop.lastHandler();

        // Handler is not called before initialDelayMs
        scheduledEventHandler.action();
        assertEquals(0, handler.actionCount());

        closeable.close();

        // Handler is NOT called after initialDelayMs because it was cancelled, but InvalidEventHandlerExceptionIsThrown
        final long firstCallTime = submittedTime + INITIAL_DELAY_MS + 1;
        timeProvider.currentTimeMillis(firstCallTime);
        assertThrows(InvalidEventHandlerException.class, () -> scheduledEventHandler.action());
        assertEquals(0, runnable.runCount());
    }
}
