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
 * The CleaningThread class extends the Thread class and provides functionality
 * to clean up thread-local variables when the thread completes its execution.
 * It is particularly useful for avoiding memory leaks associated with thread-local variables.
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
     * Cleans up any {@link CleaningThreadLocal} associated with the given thread.
     * This method uses reflection to find the thread locals for a thread and navigates through a
     * {@link WeakReference} to get to its destination. Note that if a garbage collection has occurred,
     * this method may not be able to clean up effectively.
     *
     * @param thread The thread whose CleaningThreadLocal instances are to be cleaned up.
     */
    public static void performCleanup(Thread thread) {
        performCleanup(thread, null);
    }

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
     * Checks if the given thread is an instance of CleaningThread and if it is in an event loop.
     *
     * @param t The thread to check.
     * @return true if the thread is an instance of CleaningThread and in an event loop, false otherwise.
     */
    public static boolean inEventLoop(Thread t) {
        return t instanceof CleaningThread && ((CleaningThread) t).inEventLoop();
    }

    /**
     * Overrides the run method to reset the thread affinity, execute the target runnable
     * and then perform clean up of thread locals.
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
     * Returns the inEventLoop flag, indicating whether this thread is in an event loop.
     *
     * @return true if this thread is in an event loop, false otherwise.
     */
    public boolean inEventLoop() {
        return inEventLoop;
    }
}
