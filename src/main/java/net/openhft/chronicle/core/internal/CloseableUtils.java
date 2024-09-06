/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.*;
import net.openhft.chronicle.core.threads.CleaningThread;
import net.openhft.chronicle.core.threads.CleaningThreadLocal;
import net.openhft.chronicle.core.util.WeakIdentityHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.nio.channels.ServerSocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for managing {@link Closeable} resources and related operations.
 * <p>
 * This class provides methods for tracking, managing, and waiting for closeable resources to close.
 * It enables tracing of resources to ensure proper resource management, especially in environments
 * where resources need to be explicitly closed.
 * </p>
 * <p>
 * The class is designed for internal use and assists in handling cleanup tasks such as garbage collection
 * and resource finalization, ensuring that resources are properly released before continuing execution.
 * </p>
 * <p>
 * This class cannot be instantiated and is intended to provide static utility methods.
 * </p>
 */
public final class CloseableUtils {
    /**
     * Atomic reference to a set of closeable resources being tracked.
     * <p>
     * NOTE: This assumes that the collection will not be replaced concurrently, and its lifecycle
     * is managed in a single-threaded manner between tests. The set itself may be modified concurrently.
     * </p>
     */
    private static final AtomicReference<Set<Closeable>> CLOSEABLES = new AtomicReference<>();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CloseableUtils() {
    }

    /**
     * Adds a closeable resource to the set of tracked resources.
     * <p>
     * If tracing is enabled, the closeable resource is added to the set for monitoring purposes.
     * This method should be called whenever a closeable resource is created and needs to be tracked.
     * </p>
     *
     * @param closeable The closeable resource to add to the tracking set.
     */
    public static void add(Closeable closeable) {
        final Set<Closeable> set = CLOSEABLES.get();
        if (set != null)
            set.add(closeable);
    }

    /**
     * Enables tracing of closeable resources.
     * <p>
     * Tracked resources will be stored in a {@link WeakIdentityHashMap}, ensuring that they are
     * monitored for proper closure and release. This method should be called before using closeable resources.
     * </p>
     */
    public static void enableCloseableTracing() {
        CLOSEABLES.set(
                Collections.synchronizedSet(
                        Collections.newSetFromMap(
                                new WeakIdentityHashMap<>())));
    }

    /**
     * Disables tracing of closeable resources.
     * <p>
     * After this method is called, closeable resources will no longer be tracked, and any existing
     * tracked resources will be cleared from the tracking set.
     * </p>
     */
    public static void disableCloseableTracing() {
        CLOSEABLES.set(null);
    }

    /**
     * Performs garbage collection and waits for closeables to close.
     * <p>
     * This method ensures that all tracked closeable resources are properly closed. It performs the following steps:
     * <ul>
     *     <li>Performs cleanup on the current thread using {@link CleaningThread#performCleanup(Thread)}</li>
     *     <li>Triggers garbage collection to finalize objects</li>
     *     <li>Waits for closeables to close with a specified timeout</li>
     *     <li>Verifies that the finalizer thread has processed any finalizable objects</li>
     * </ul>
     * If the finalizer thread does not complete within the specified timeout, it throws an {@link AssertionError}.
     *
     * @throws AssertionError If the finalizer does not complete within the specified timeout.
     */
    public static void gcAndWaitForCloseablesToClose() {
        // Perform cleanup on the current thread
        CleaningThread.performCleanup(Thread.currentThread());

        // Queue to track finalization of anonymous objects
        final BlockingQueue<String> q = new LinkedBlockingQueue<>();

        // Anonymous object to track finalization using finalize() method
        new Object() {
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                q.add("finalized");
            }
        };

        try {
            // Trigger garbage collection and wait for finalization
            for (int i = 1; i <= 10; i++) {
                System.gc();
                if (q.poll(500, TimeUnit.MILLISECONDS) != null)
                    break;

                if (i == 10)
                    throw new AssertionError("Timed out waiting for the Finalizer");
            }

            // Wait for closeables to close
            AbstractCloseable.waitForCloseablesToClose(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    /**
     * Waits for closeable resources to close within a specified time limit.
     * <p>
     * This method checks the closeable resources in the tracking set to determine if they have been closed.
     * If all resources are closed within the specified time limit, it returns {@code true}. If the time limit
     * is exceeded before all resources are closed, an {@link IllegalStateException} is thrown.
     * </p>
     *
     * @param millis The time limit in milliseconds to wait for the closeable resources to close.
     * @return {@code true} if all closeable resources are closed within the time limit, {@code false} otherwise.
     */
    public static boolean waitForCloseablesToClose(long millis) {
        final Set<Closeable> traceSet = CLOSEABLES.get();
        if (traceSet == null) {
            return true;
        }
        if (Thread.interrupted())
            System.err.println("Interrupted in waitForCloseablesToClose!");

        long end = System.currentTimeMillis() + millis;

        toWait:
        while (true) {
            Collection<Closeable> traceSetCopy;
            synchronized (traceSet) {
                traceSetCopy = new ArrayList<>(traceSet);
            }
            for (Closeable key : traceSetCopy) {
                if (key.isClosing())
                    continue;
                try {
                    // Disable single-threaded checks and ensure resources are released
                    if (key instanceof AbstractCloseable) {
                        ((AbstractCloseable) key).singleThreadedCheckDisabled(true);
                    }
                    if (key instanceof ReferenceCountedTracer) {
                        ((ReferenceCountedTracer) key).throwExceptionIfNotReleased();
                    }

                } catch (IllegalStateException e) {
                    if (System.currentTimeMillis() > end)
                        throw e;

                    // Release pending resources and perform cleanup
                    BackgroundResourceReleaser.releasePendingResources();

                    CleaningThreadLocal.cleanupNonCleaningThreads();

                    Jvm.pause(1);
                    continue toWait;
                }
            }
            return true;
        }
    }

    /**
     * Asserts that all closeable resources are closed.
     * <p>
     * This method checks if there are any remaining open closeable resources that have not been closed.
     * It waits briefly for resources to close in the background and verifies if all tracked resources are closed.
     * If any resources remain open, an {@link AssertionError} is thrown, providing detailed information
     * about the resources that were not properly closed.
     * </p>
     *
     * @throws AssertionError If any resources remain open after the assertion check.
     */
    public static void assertCloseablesClosed() {
        final Set<Closeable> traceSet = CLOSEABLES.get();
        if (traceSet == null) {
            Jvm.warn().on(AbstractCloseable.class, "closable tracing disabled");
            return;
        }
        if (Thread.interrupted())
            System.err.println("Interrupted in assertCloseablesClosed!");

        BackgroundResourceReleaser.releasePendingResources();

        AssertionError openFiles = new AssertionError("Closeables still open");

        synchronized (traceSet) {
            Set<Closeable> traceSet2 = Collections.newSetFromMap(new IdentityHashMap<>());
            if (waitForTraceSet(traceSet, traceSet2))
                return;

            captureTheUnclosed(openFiles, traceSet2);
        }

        if (openFiles.getSuppressed().length > 0) {
            throw openFiles;
        }
    }

    /**
     * Waits for closeable resources to close by periodically checking their status.
     * If all resources are closed, the method returns {@code true}.
     * <p>
     * It checks the state of closeable resources in the provided trace set and waits briefly for resources
     * to close in the background. If resources close within the allotted time (250 milliseconds), the method
     * returns {@code true}, indicating success.
     * </p>
     *
     * @param traceSet  The set of tracked closeable resources.
     * @param traceSet2 A temporary set for tracking nested resources.
     * @return {@code true} if all closeable resources are closed, {@code false} otherwise.
     */
    private static boolean waitForTraceSet(Set<Closeable> traceSet, Set<Closeable> traceSet2) {
        traceSet.removeIf(o -> o == null || o.isClosing());
        Set<Closeable> nested = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Closeable key : traceSet) {
            addNested(nested, key, 1);
        }
        traceSet2.addAll(traceSet);
        traceSet2.removeAll(nested);

        // wait up to 250 ms for resources to be closed in the background.
        for (int i = 0; i < 250; i++) {
            if (traceSet2.stream().allMatch(Closeable::isClosing))
                return true;
            Jvm.pause(1);
        }
        return false;
    }

    /**
     * Captures information about unclosed resources.
     * <p>
     * This method adds detailed information about the resources that have not been closed,
     * including where they were created or held references. It closes the unclosed resources and
     * adds exceptions to the provided {@link AssertionError}.
     * </p>
     *
     * @param openFiles The {@link AssertionError} to which unclosed resources are added.
     * @param traceSet2 The set of closeable resources that remain unclosed.
     */
    private static void captureTheUnclosed(AssertionError openFiles, Set<Closeable> traceSet2) {
        for (Closeable key : traceSet2) {
            Throwable t = null;
            try {
                if (key instanceof ReferenceCountedTracer) {
                    ((ReferenceCountedTracer) key).throwExceptionIfNotReleased();
                }
                if (key instanceof ManagedCloseable) {
                    t = ((ManagedCloseable) key).createdHere();
                }
            } catch (IllegalStateException e) {
                t = e;
            }
            IllegalStateException exception = new IllegalStateException("Not closed " + asString(key), t);
            if (key.isClosed()) {
                continue;
            }
            openFiles.addSuppressed(exception);
            key.close();
        }
    }

    /**
     * Recursively adds nested closeable resources to the provided set for tracking.
     * <p>
     * This method searches for nested closeable resources within the fields of the given resource and adds them
     * to the tracking set if they are not already closing. It also handles recursive nesting of resources.
     * </p>
     *
     * @param nested The set of nested closeable resources to track.
     * @param key    The top-level closeable resource.
     * @param depth  The depth of recursion to search for nested resources.
     */
    private static void addNested(Set<Closeable> nested, Closeable key, int depth) {
        if (key.isClosing())
            return;
        Set<Field> fields = new HashSet<>();
        Class<? extends Closeable> keyClass = key.getClass();
        getCloseableFields(keyClass, fields);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Closeable o = (Closeable) field.get(key);
                if (o != null && nested.add(o) && depth > 1)
                    addNested(nested, o, depth - 1);
            } catch (IllegalAccessException e) {
                Jvm.warn().on(keyClass, e);
            }
        }
    }

    /**
     * Retrieves all {@link Closeable} fields from the given class and its superclasses.
     *
     * @param keyClass The class to inspect for {@link Closeable} fields.
     * @param fields   The set to which the found fields are added.
     */
    private static void getCloseableFields(Class<?> keyClass, Set<Field> fields) {
        if (keyClass == null || keyClass == Object.class)
            return;
        for (Field field : keyClass.getDeclaredFields())
            if (Closeable.class.isAssignableFrom(field.getType()))
                fields.add(field);
        getCloseableFields(keyClass.getSuperclass(), fields);
    }

    /**
     * Removes the specified closeable from monitoring.
     * <p>
     * This method is useful if a resource should no longer be tracked as a closeable resource.
     * </p>
     *
     * @param closeable The closeable resource to remove from monitoring.
     */
    public static void unmonitor(Closeable closeable) {
        final Set<Closeable> set = CLOSEABLES.get();
        if (set != null)
            set.remove(closeable);
    }

    /**
     * Closes multiple closeable resources quietly without throwing an exception.
     * <p>
     * If the resource is a collection or array, this method will close all elements within it.
     * </p>
     *
     * @param closeables The closeable resources to close.
     */
    public static void closeQuietly(@Nullable Object... closeables) {
        if (closeables == null)
            return;
        for (Object o : closeables)
            closeQuietly(o);
    }

    /**
     * Closes a single closeable resource quietly without throwing an exception.
     * <p>
     * This method handles collections, arrays, {@link ServerSocketChannel}, {@link HttpURLConnection}, and other
     * {@link AutoCloseable} resources. It logs any errors encountered during the closing operation.
     * </p>
     *
     * @param o The object to close.
     */
    static void closeQuietly(@Nullable Object o) {
        if (o instanceof Collection) {
            Collection coll = (Collection) o;
            if (coll.isEmpty())
                return;
            // take a copy before removing
            List list = new ArrayList<>(coll);
            list.forEach(Closeable::closeQuietly);

        } else if (o instanceof Object[]) {
            for (Object o2 : (Object[]) o)
                closeQuietly(o2);

        } else if (o instanceof ServerSocketChannel) {
            try {
                ((ServerSocketChannel) o).close();
            } catch (IOException e) {
                // If you close a ServerSocketChannelImpl more than once it can throw an IOException that it doesn't exist.
                if (!"No such file or directory".equals(e.getMessage()))
                    logErrorOnClose(e);
            } catch (Throwable e) {
                logErrorOnClose(e);
            }

        } else if (o instanceof java.lang.AutoCloseable) {
            try {
                ((java.lang.AutoCloseable) o).close();
            } catch (Throwable e) {
                logErrorOnClose(e);
            }

        } else if (o instanceof Reference) {
            closeQuietly(((Reference) o).get());

        } else if (o instanceof HttpURLConnection) {
            HttpURLConnection connection = (HttpURLConnection) o;
            connection.disconnect();
        }
    }

    /**
     * Logs any errors encountered during the closing of resources.
     *
     * @param e The exception or error that occurred during closing.
     */
    static void logErrorOnClose(Throwable e) {
        Jvm.warn().on(Closeable.class, "Error occurred closing resources", e);
    }

    /**
     * Returns a string representation of the closeable or reference owner.
     *
     * @param id The object to represent as a string.
     * @return A string representation of the object, including its reference count and closed status if applicable.
     */
    public static String asString(Object id) {
        if (id == ReferenceOwner.INIT) return "INIT";
        String s = id instanceof ReferenceOwner
                ? ((ReferenceOwner) id).referenceName()
                : id.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(id));
        if (id instanceof ReferenceCounted)
            s += " refCount=" + ((ReferenceCounted) id).refCount();
        try {
            if (id instanceof QueryCloseable)
                s += " closed=" + ((QueryCloseable) id).isClosed();
        } catch (NullPointerException ignored) {
            // not initialised
        }
        return s;
    }
}
