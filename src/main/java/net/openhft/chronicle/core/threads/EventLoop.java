/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;

/**
 * EventLoop represents an event-driven loop responsible for processing {@link EventHandler EventHandlers}.
 * <p>
 * Implementations are <em>not</em> re-entrant. All state changes must occur on
 * the event-loop thread to avoid race conditions. Typical usage is to add
 * handlers before calling {@link #start()} and then interact with the loop only
 * from within its own thread.
 * <p>
 * The {@link #runsInsideCoreLoop()} method allows callers to detect if the
 * current thread is the core event thread. Code already running on the loop can
 * call handlers directly rather than using an {@code invokeAndWait}-style
 * mechanism.
 * <p>
 * Calling {@link #addHandler(EventHandler)} after {@link #stop()} results in an
 * {@link IllegalStateException}.
 * <p>
 * {@link HandlerPriority Handler priorities} control the relative frequency of
 * handler invocation. The following table shows the approximate call frequency
 * compared with {@code HIGH} priority:
 * <pre>
 * Priority            Relative frequency
 * HIGH                1 (baseline)
 * MEDIUM              ~1/4 of HIGH
 * TIMER               ~1/16 of HIGH
 * DAEMON              only when idle
 * MONITOR             background thread
 * BLOCKING            separate cached thread pool
 * REPLICATION         alias of MEDIUM
 * REPLICATION_TIMER   alias of TIMER
 * CONCURRENT          alias of MEDIUM
 * </pre>
 */
public interface EventLoop extends Closeable {

    boolean DEBUG_ADDING_HANDLERS = Jvm.getBoolean("debug.adding.handlers");
    boolean DEBUG_REMOVING_HANDLERS = Jvm.getBoolean("debug.removing.handlers");

    /**
     * Retrieves the name of the event loop.
     *
     * @return the name of the event loop.
     */
    String name();

    /**
     * Adds a handler to the event loop to be executed. The event loop should execute
     * handlers in order of their priority. Handlers with the same priority have no
     * guarantee of execution order. Handlers will not be executed before {@link #start()}
     * has been called.
     *
     * @param handler The handler to be added to the event loop.
     * @throws IllegalStateException if the event loop has been {@link #stop() stopped}
     *                               or {@link #close() closed}.
     */
    void addHandler(EventHandler handler);

    /**
     * Starts the event loop. Once the event loop is started, it begins executing handlers.
     */
    void start();

    /**
     * Typically, implementations will unpause the event loop's pauser, if used.
     * This can be helpful in cases where the event loop was temporarily paused for some reason.
     */
    void unpause();

    /**
     * Stops executing handlers and blocks until all handlers are complete.
     * It is not expected that event loops can then be restarted.
     */
    void stop();

    /**
     * Checks if the main thread of the event loop is running.
     *
     * @return {@code true} if the main thread is running, otherwise {@code false}.
     */
    boolean isAlive();

    /**
     * Checks if the event loop is in the stopped state.
     *
     * @return {@code true} if the event loop is in the stopped state, otherwise {@code false}.
     */
    boolean isStopped();

    /**
     * Checks if the current thread is executing inside an event loop.
     *
     * @return {@code true} if the current thread is executing inside an event loop, otherwise {@code false}.
     */
    static boolean inEventLoop() {
        return CleaningThread.inEventLoop(Thread.currentThread());
    }

    /**
     * Stops the event loop and then closes any resources being held.
     * Blocks until all the handlers are stopped and closed.
     */
    @Override
    void close();

    /**
     * Checks if the current thread is the core thread of this event loop, or if this information
     * cannot be determined. If the current thread is not the core event thread but any other,
     * this method returns {@code false}.
     *
     * @return {@code true} unless the current thread is not the core event thread.
     */
    default boolean runsInsideCoreLoop() {
        return true;
    }
}
