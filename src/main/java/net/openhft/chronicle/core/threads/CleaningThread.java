/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static net.openhft.chronicle.core.Jvm.isResourceTracing;
import static net.openhft.chronicle.core.Jvm.uncheckedCast;

/**
 * Thread that clears its ThreadLocal values when finished.
 * <p>
 * Threads can retain references left in their {@code ThreadLocalMap} even once
 * the {@link ThreadLocal} instance is no longer reachable. Long-lived threads
 * may therefore accumulate stale values and leak memory. Reflection is used to
 * walk the map and remove any entries created by {@link CleaningThreadLocal} so
 * they are not retained after the task completes.
 */
public class CleaningThread extends Thread {
    private static final Field THREAD_LOCALS;
    private static final Field TABLE;
    private static final Field VALUE;

    // Static block to initialize reflection fields.
    static {
        THREAD_LOCALS = Jvm.getField(Thread.class, "threadLocals");
        TABLE = Jvm.getField(THREAD_LOCALS.getType(), "table");
        VALUE = Jvm.getField(TABLE.getType().getComponentType(), "value");
    }

    private final boolean inEventLoop;
    private final StackTrace createdHere = isResourceTracing() ? new StackTrace("Created here") : null;

    /**
     * Constructs a new CleaningThread with the specified target Runnable.
     *
     * @param target The Runnable object to execute in the new thread.
     */
    public CleaningThread(Runnable target) {
        super(target);
        inEventLoop = false;
    }

    /**
     * Constructs a new CleaningThread with the specified target Runnable and name.
     *
     * @param target The Runnable object to execute in the new thread.
     * @param name   The name for the new thread.
     */
    public CleaningThread(Runnable target, String name) {
        this(target, name, false);
    }

    /**
     * Constructs a new CleaningThread with the specified target Runnable, name, and inEventLoop flag.
     *
     * @param target      The Runnable object to execute in the new thread.
     * @param name        The name for the new thread.
     * @param inEventLoop The flag indicating whether the thread is in an event loop.
     */
    public CleaningThread(Runnable target, String name, boolean inEventLoop) {
        super(target, name);
        this.inEventLoop = inEventLoop;
    }

    /**
     * Purges all {@link CleaningThreadLocal} entries for the supplied thread.
     * <p>
     * Thread-local maps hold strong references to their values until removed. If a
     * task discards its {@link CleaningThreadLocal} but the thread lives on, those
     * values leak. This method walks the hidden map via reflection and removes the
     * entries we created. If garbage collection has cleared a weak key the value
     * cannot be located and remains until the table is resized.
     *
     * @param thread the thread whose {@code CleaningThreadLocal} instances are to be
     *               cleared
     */
    public static void performCleanup(Thread thread) {
        performCleanup(thread, null);
    }

    @Nullable
    @SuppressWarnings("java:S3011") // Justification: non-public remove(ThreadLocal) is required to clean thread-locals deterministically.
    private static Method getRemoveMethod(Object o) {
        Method remove;
        try {
            remove = o.getClass().getDeclaredMethod("remove", ThreadLocal.class);
            remove.setAccessible(true);
        } catch (NoSuchMethodException e) {
            return null;
        }
        return remove;
    }

    /**
     * Cleans up a specific {@link CleaningThreadLocal} instance associated with the given thread.
     *
     * @param thread The thread whose specific CleaningThreadLocal instance is to be cleaned up.
     * @param ctl    The specific CleaningThreadLocal instance to clean up. If null, cleans all.
     */
    public static void performCleanup(Thread thread, CleaningThreadLocal<?> ctl) {
        WeakReference<?>[] table;
        Object o;
        try {
            o = THREAD_LOCALS.get(thread);
            if (o == null)
                return;
            table = (WeakReference<?>[]) TABLE.get(o);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            Jvm.debug().on(CleaningThreadLocal.class, e.toString());
            return;
        }
        if (table == null)
            return;

        Method remove = getRemoveMethod(o);
        if (remove == null) return;

        scanReferences(ctl, table, o, remove);
    }

    /**
     * Iterates through the references in the table, cleaning up and removing the CleaningThreadLocal instances.
     */
    private static void scanReferences(CleaningThreadLocal<?> ctl, WeakReference<?>[] table, Object o, Method remove) {
        for (WeakReference<?> reference : table.clone()) {
            try {
                Object key = reference != null ? reference.get() : null;
                if (!(key instanceof CleaningThreadLocal) || (ctl != null && key != ctl))
                    continue;

                Object value = VALUE.get(reference);
                if (value == null)
                    continue;

                CleaningThreadLocal<Object> ctlKey = uncheckedCast(key);
                ctlKey.cleanup(value);

                remove.invoke(o, key);
                if (ctl != null)
                    break;
            } catch (IllegalAccessException e) {
                Jvm.debug().on(CleaningThreadLocal.class, e.toString());
            } catch (Throwable e) {
                Jvm.debug().on(CleaningThreadLocal.class, e);
            }
        }
    }

    /**
     * Checks if the given thread is an instance of CleaningThread and if it is in an event loop.
     *
     * @param t The thread to check.
     * @return true if the thread is an instance of CleaningThread and in an event loop, false otherwise.
     */
    public static boolean inEventLoop(Thread t) {
        return t instanceof CleaningThread && ((CleaningThread) t).inEventLoop();
    }

    /**
     * Executes the target {@link Runnable} and then clears thread-local values.
     * <p>
     * If an event loop pinned this thread to a single CPU, the affinity is
     * reset to {@link AffinityLock#BASE_AFFINITY} before user code runs so the
     * binding does not leak once the loop has finished.
     *
     * <p> Sub-classes overriding this method should call
     * {@code super.run()} to retain the affinity reset and cleanup behaviour.
     */
    @Override
    public void run() {
        // ensure the logger has loaded before attempting to access affinity
        Jvm.debug().isEnabled(getClass());

        // Reset thread affinity if required
        if (Affinity.getAffinity().cardinality() == 1) {
            Jvm.debug().on(getClass(), "Resetting affinity from " + Affinity.getAffinity() + " to " + AffinityLock.BASE_AFFINITY);
            Affinity.setAffinity(AffinityLock.BASE_AFFINITY);
        }

        try {
            super.run();
        } finally {
            performCleanup(this);
        }
    }

    /**
     * Returns the inEventLoop flag, indicating whether this thread is in an event loop.
     *
     * @return true if this thread is in an event loop, false otherwise.
     */
    public boolean inEventLoop() {
        return inEventLoop;
    }

    /**
     * @return where the Thread was created, if available
     */
    public StackTrace createdHere() {
        return createdHere;
    }
}
