//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
/**
 * Cooperative single-thread event processing utilities.
 *
 * <p>An {@link net.openhft.chronicle.core.threads.EventLoop EventLoop} runs each
 * {@link net.openhft.chronicle.core.threads.EventHandler} on its dedicated core
 * thread. Successive calls to {@code action()} occur on the same thread and thus
 * form a happens-before relationship.
 *
 * <p>The {@link net.openhft.chronicle.core.threads.HandlerPriority} enum orders
 * handlers. Some values are aliases &ndash; for example {@code REPLICATION}
 * resolves to {@code MEDIUM}. Implementations use the value returned by
 * {@link net.openhft.chronicle.core.threads.HandlerPriority#alias()}.
 *
 * <p>Affinity can pin the core thread to a specific processor using the
 * Java&nbsp;Thread&nbsp;Affinity library. {@link net.openhft.chronicle.core.threads.CleaningThread}
 * resets the affinity to {@link net.openhft.affinity.AffinityLock#BASE_AFFINITY}
 * after running the task.
 *
 * <p>Parameters and return values are non-null unless annotated with
 * {@link org.jetbrains.annotations.Nullable}.
 *
 * <p> Handlers should perform small units of work and return promptly.
 * <p> Implementations must honour the alias mapping defined by
 * {@code HandlerPriority}.
 * <p> The default loop executes on a {@code CleaningThread} which also
 * clears thread-local state.
 *
 * <pre>{@code
 * EventLoop loop = ...;
 * loop.start();
 * Timer timer = new Timer(loop);
 * java.io.Closeable handle = timer.scheduleAtFixedRate(() -> {
 *     System.out.println("tick");
 *     return false;
 * }, 0, 100);
 * handle.close();
 * loop.stop();
 * loop.close();
 * }</pre>
 */
package net.openhft.chronicle.core.threads;
