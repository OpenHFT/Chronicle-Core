package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * This will clean up any {@link CleaningThreadLocal}
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

    public CleaningThread(Runnable target) {
        super(target);
    }

    public CleaningThread(Runnable target, String name) {
        super(target, name);
    }

    public static void performCleanup(Thread thread) {
        WeakReference[] table;
        try {
            Object o = THREAD_LOCALS.get(thread);
            if (o == null)
                return;
            table = (WeakReference[]) TABLE.get(o);
        } catch (IllegalAccessException e) {
            Jvm.debug().on(CleaningThreadLocal.class, e.toString());
            return;
        }
        if (table == null)
            return;

        for (WeakReference reference : table) {
            Object key = reference != null ? reference.get() : null;
            if (!(key instanceof CleaningThreadLocal))
                continue;

            try {
                Object value = VALUE.get(reference);
                if (value == null)
                    continue;

                CleaningThreadLocal ctl = (CleaningThreadLocal) key;
                ctl.cleanup(value);
            } catch (IllegalAccessException e) {
                Jvm.debug().on(CleaningThreadLocal.class, e.toString());
            }
        }
    }

    @Override
    public void run() {
        try {
            super.run();
        } finally {
            performCleanup(this);
        }
    }
}
