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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This will clean up any {@link CleaningThreadLocal} when it completes
 */
public class CleaningThread extends Thread {
    private static final Field THREAD_LOCALS;
    private static final Field TABLE;
    private static final Field VALUE;

    static {
        THREAD_LOCALS = Jvm.getField(Thread.class, "threadLocals");
        TABLE = Jvm.getField(THREAD_LOCALS.getType(), "table");
        VALUE = Jvm.getField(TABLE.getType().getComponentType(), "value");
    }

    private final boolean inEventLoop;

    public CleaningThread(Runnable target) {
        super(target);
        inEventLoop = false;
    }

    public CleaningThread(Runnable target, String name) {
        this(target, name, false);
    }

    public CleaningThread(Runnable target, String name, boolean inEventLoop) {
        super(target, name);
        this.inEventLoop = inEventLoop;
    }

    /**
     * Clean up any {@link CleaningThreadLocal} associated with the passed thread.
     * <p>
     * This method uses reflection to find the thread locals for a thread and navigates through a
     * {@link WeakReference} to get to its destination, so if a GC has occurred then this may not be able
     * to clean up effectively.
     *
     * @param thread thread to clean for
     */
    public static void performCleanup(Thread thread) {
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

        Method remove;
        try {
            remove = o.getClass().getDeclaredMethod("remove", ThreadLocal.class);
            remove.setAccessible(true);
        } catch (NoSuchMethodException e) {
            return;
        }

        for (WeakReference<?> reference : table.clone()) {
            try {
                Object key = reference != null ? reference.get() : null;
                if (!(key instanceof CleaningThreadLocal))
                    continue;

                Object value = VALUE.get(reference);
                if (value == null)
                    continue;

                @SuppressWarnings("unchecked") final CleaningThreadLocal<Object> ctl = (CleaningThreadLocal<Object>) key;
                ctl.cleanup(value);

                remove.invoke(o, key);
            } catch (IllegalAccessException e) {
                Jvm.debug().on(CleaningThreadLocal.class, e.toString());
            } catch (Throwable e) {
                Jvm.debug().on(CleaningThreadLocal.class, e);
            }
        }
    }

    /**
     * Cleanup a specific CleaningThreadLocal
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

        Method remove;
        try {
            remove = o.getClass().getDeclaredMethod("remove", ThreadLocal.class);
            remove.setAccessible(true);
        } catch (NoSuchMethodException e) {
            return;
        }

        for (WeakReference<?> reference : table) {
            try {
                Object key = reference != null ? reference.get() : null;
                if (!(key instanceof CleaningThreadLocal) || key != ctl)
                    continue;

                Object value = VALUE.get(reference);
                if (value == null)
                    continue;

                ((CleaningThreadLocal<Object>) ctl).cleanup(value);

                remove.invoke(o, key);
                break;
            } catch (IllegalAccessException e) {
                Jvm.debug().on(CleaningThreadLocal.class, e.toString());
            } catch (Throwable e) {
                Jvm.debug().on(CleaningThreadLocal.class, e);
            }
        }
    }

    public static boolean inEventLoop(Thread t) {
        return t instanceof CleaningThread && ((CleaningThread) t).inEventLoop();
    }

    @Override
    public void run() {
        // reset the thread affinity
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

    public boolean inEventLoop() {
        return inEventLoop;
    }
}
