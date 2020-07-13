package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;

import java.lang.ref.WeakReference;

/**
 * This will clean up any {@link CleaningThreadLocal}
 */
public class CleaningThread extends Thread {
    public CleaningThread(Runnable target) {
        super(target);
    }

    public CleaningThread(Runnable target, String name) {
        super(target, name);
    }

    public static void performCleanup(Thread thread) {
        WeakReference[] table = Jvm.getValue(thread, "threadLocals/table");
        if (table != null) {
            for (WeakReference reference : table) {
                Object key = reference != null ? reference.get() : null;
                if (key instanceof CleaningThreadLocal) {
                    Object value = Jvm.getValue(reference, "value");
                    if (value != null) {
                        CleaningThreadLocal ctl = (CleaningThreadLocal) key;
                        ctl.cleanup(value);
                    }
                }
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
