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
 * Utility class for managing closeable resources and related operations.
 */
public final class CloseableUtils {
    /**
     * Set of closeable resources being tracked.
     * <p>
     * NOTE: This assumes the collection will not be replaced concurrently, and a particular lifecycle is used.
     * It is set and reset between tests in a single threaded manner. The set itself could be changed concurrently.
     */
    private static final AtomicReference<Set<Closeable>> CLOSEABLES = new AtomicReference<>();

    private CloseableUtils() {
    }

    /**
     * Adds a closeable resource to the set of tracked resources.
     * If tracing is enabled, the closeable resource is added to the set for monitoring purposes.
     *
     * @param closeable The closeable resource to add.
     */
    public static void add(Closeable closeable) {
        final Set<Closeable> set = CLOSEABLES.get();
        if (set != null)
            set.add(closeable);
    }

    /**
     * Enables tracing of closeable resources.
     * Tracked resources will be stored in a set.
     * This method should be called to enable tracing before using closeable resources.
     */
    public static void enableCloseableTracing() {
        CLOSEABLES.set(
                Collections.synchronizedSet(
                        Collections.newSetFromMap(
                                new WeakIdentityHashMap<>())));
    }

    /**
     * Disables tracing of closeable resources.
     * Tracked resources will no longer be stored in the set.
     * This method should be called to disable tracing when no longer needed.
     */
    public static void disableCloseableTracing() {
        CLOSEABLES.set(null);
    }

    /**
     * Performs garbage collection and waits for closeables to close.
     * This method is useful for ensuring that closeable resources are properly closed before continuing.
     * It performs the following steps:
     * 1. Performs cleanup on the current thread.
     * 2. Performs garbage collection.
     * 3. Waits for closeables to close with a specified timeout.
     * 4. Checks if the finalizer thread has finalized any objects.
     * If not, it throws an AssertionError.
     *
     * @throws AssertionError If the finalizer does not complete within the specified timeout.
     */
    @SuppressWarnings("removal")
    public static void gcAndWaitForCloseablesToClose() {
        CleaningThread.performCleanup(Thread.currentThread());

        // find any discarded resources.
        final BlockingQueue<String> q = new LinkedBlockingQueue<>();

        // Anonymous inner class overriding the finalize() method to track finalization.
        new Object() {
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                q.add("finalized");
            }
        };

        try {
            // Zing JVM is not always satisfied with a single GC call:
            for (int i = 1; i <= 10; i++) {
                System.gc();
                if (q.poll(500, TimeUnit.MILLISECONDS) != null)
                    break;

                if (i == 10)
                    throw new AssertionError("Timed out waiting for the Finalizer");
            }

            AbstractCloseable.waitForCloseablesToClose(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    /**
     * Waits for closeable resources to close within a specified time limit.
     * This method checks if the closeable resources in the trace set have been closed.
     * If all resources are closed within the specified time limit, it returns true.
     * If the time limit is exceeded before all resources are closed, it returns false.
     *
     * @param millis The time limit in milliseconds to wait for the closeable resources to close.
     * @return true if all closeable resources are closed within the time limit, false otherwise.
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
                    // too late to be checking thread safety.
                    if (key instanceof AbstractCloseable) {
                        ((AbstractCloseable) key).singleThreadedCheckDisabled(true);
                    }
                    if (key instanceof ReferenceCountedTracer) {
                        ((ReferenceCountedTracer) key).throwExceptionIfNotReleased();
                    }

                } catch (IllegalStateException e) {
                    if (System.currentTimeMillis() > end)
                        throw e;

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
     * This method checks if there are any remaining open closeable resources.
     * If any resources are found to be open, an AssertionError is thrown.
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

    private static void getCloseableFields(Class<?> keyClass, Set<Field> fields) {
        if (keyClass == null || keyClass == Object.class)
            return;
        for (Field field : keyClass.getDeclaredFields())
            if (Closeable.class.isAssignableFrom(field.getType()))
                fields.add(field);
        getCloseableFields(keyClass.getSuperclass(), fields);
    }

    /**
     * @param closeable to remove monitoring of
     */
    public static void unmonitor(Closeable closeable) {
        final Set<Closeable> set = CLOSEABLES.get();
        if (set != null)
            set.remove(closeable);
    }

    /**
     * Close a closeable quietly, i.e. without throwing an exception.
     * If the closeable is a collection, close all the elements.
     * If the closeable is an array, close all the elements.
     * If the closeable is a ServerSocketChannel, close it quietly.
     *
     * @param closeables the objects to close
     */
    public static void closeQuietly(@Nullable Object... closeables) {
        if (closeables == null)
            return;
        for (Object o : closeables)
            closeQuietly(o);
    }

    /**
     * Close a closeable quietly, i.e. without throwing an exception.
     * If the closeable is a collection, close all the elements.
     * If the closeable is an array, close all the elements.
     * If the closeable is a ServerSocketChannel, close it quietly.
     *
     * @param o the object to close
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

    static void logErrorOnClose(Throwable e) {
        Jvm.warn().on(Closeable.class, "Error occurred closing resources", e);
    }

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
