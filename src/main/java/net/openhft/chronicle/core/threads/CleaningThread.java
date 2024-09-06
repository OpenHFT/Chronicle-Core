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

/**
 * The {@code CleaningThread} class extends the {@link Thread} class and provides functionality
 * to clean up thread-local variables when the thread completes its execution.
 * It is particularly useful for avoiding memory leaks associated with thread-local variables,
 * especially when these are not automatically cleaned up by the garbage collector.
 */
public class CleaningThread extends Thread {
    private static final Field THREAD_LOCALS;
    private static final Field TABLE;
    private static final Field VALUE;

    private final boolean inEventLoop;
    @SuppressWarnings("unused")
    private final StackTrace createdHere = isResourceTracing() ? new StackTrace("Created here") : null;

    // Static block to initialize reflection fields.
    static {
        THREAD_LOCALS = Jvm.getField(Thread.class, "threadLocals");
        TABLE = Jvm.getField(THREAD_LOCALS.getType(), "table");
        VALUE = Jvm.getField(TABLE.getType().getComponentType(), "value");
    }

    /**
     * Constructs a new {@code CleaningThread} with the specified target {@link Runnable}.
     *
     * @param target The {@link Runnable} object to execute in the new thread.
     */
    public CleaningThread(Runnable target) {
        super(target);
        inEventLoop = false;
    }

    /**
     * Constructs a new {@code CleaningThread} with the specified target {@link Runnable} and name.
     *
     * @param target The {@link Runnable} object to execute in the new thread.
     * @param name   The name for the new thread.
     */
    public CleaningThread(Runnable target, String name) {
        this(target, name, false);
    }

    /**
     * Constructs a new {@code CleaningThread} with the specified target {@link Runnable}, name, and inEventLoop flag.
     *
     * @param target      The {@link Runnable} object to execute in the new thread.
     * @param name        The name for the new thread.
     * @param inEventLoop The flag indicating whether the thread is in an event loop.
     */
    public CleaningThread(Runnable target, String name, boolean inEventLoop) {
        super(target, name);
        this.inEventLoop = inEventLoop;
    }

    /**
     * Cleans up any {@link CleaningThreadLocal} associated with the given thread.
     * This method uses reflection to find the thread locals for a thread and navigates through a
     * {@link WeakReference} to get to its destination. Note that if a garbage collection has occurred,
     * this method may not be able to clean up effectively.
     *
     * @param thread The thread whose {@link CleaningThreadLocal} instances are to be cleaned up.
     */
    public static void performCleanup(Thread thread) {
        performCleanup(thread, null);
    }

    /**
     * Retrieves the {@code remove} method for a given object, if it exists.
     * This method is used to remove a specific {@link ThreadLocal} from a thread's local variables.
     *
     * @param o The object for which the {@code remove} method is being retrieved.
     * @return The {@code remove} method if found, otherwise {@code null}.
     */
    @Nullable
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
     * @param thread The thread whose specific {@link CleaningThreadLocal} instance is to be cleaned up.
     * @param ctl    The specific {@link CleaningThreadLocal} instance to clean up. If {@code null}, cleans all.
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
     * Iterates through the references in the table, cleaning up and removing the {@link CleaningThreadLocal} instances.
     *
     * @param ctl    The specific {@link CleaningThreadLocal} instance to clean up, or {@code null} to clean all.
     * @param table  The table of {@link WeakReference} entries to iterate over.
     * @param o      The thread local map object from which to remove entries.
     * @param remove The method used to remove entries from the thread local map.
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

                CleaningThreadLocal<Object> ctlKey = (CleaningThreadLocal<Object>) key;
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
     * Checks if the given thread is an instance of {@code CleaningThread} and if it is in an event loop.
     *
     * @param t The thread to check.
     * @return {@code true} if the thread is an instance of {@code CleaningThread} and in an event loop, {@code false} otherwise.
     */
    public static boolean inEventLoop(Thread t) {
        return t instanceof CleaningThread && ((CleaningThread) t).inEventLoop();
    }

    /**
     * Overrides the {@link Thread#run()} method to reset the thread affinity, execute the target {@link Runnable},
     * and then perform cleanup of thread-local variables.
     */
    @Override
    public void run() {
        // Reset thread affinity if required
        if (Affinity.getAffinity().cardinality() == 1) {
            Jvm.debug().on(getClass(), "Resetting affinity from " + Affinity.getAffinity() + " to " + AffinityLock.BASE_AFFINITY);
            Affinity.setAffinity(AffinityLock.BASE_AFFINITY);
        }

        // Execute the target Runnable and perform cleanup
        try {
            super.run();
        } finally {
            performCleanup(this);
        }
    }

    /**
     * Returns the {@code inEventLoop} flag, indicating whether this thread is in an event loop.
     *
     * @return {@code true} if this thread is in an event loop, {@code false} otherwise.
     */
    public boolean inEventLoop() {
        return inEventLoop;
    }
}
