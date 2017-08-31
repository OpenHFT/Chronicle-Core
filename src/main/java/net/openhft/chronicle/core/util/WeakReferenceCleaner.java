package net.openhft.chronicle.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Intended as a replacement for usage of sun.misc.Cleaner/jdk.internal.ref.Cleaner
 */
public final class WeakReferenceCleaner extends WeakReference<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeakReferenceCleaner.class);
    private static final ReferenceQueue<Object> REFERENCE_QUEUE = new ReferenceQueue<>();
    private static final Map<WeakReferenceCleaner, Boolean> REFERENCE_MAP = new ConcurrentHashMap<>();
    private static final AtomicBoolean REFERENCE_PROCESSOR_STARTED = new AtomicBoolean(false);

    private final Runnable thunk;

    private WeakReferenceCleaner(final Object referent, final Runnable thunk) {
        super(referent, REFERENCE_QUEUE);
        this.thunk = thunk;
    }

    public static WeakReferenceCleaner newCleaner(final Object referent, final Runnable thunk) {
        final WeakReferenceCleaner cleaner = new WeakReferenceCleaner(referent, thunk);
        REFERENCE_MAP.put(cleaner, Boolean.TRUE);
        return cleaner;
    }

    public static void startReferenceProcessor(final Executor executor) {
        if (REFERENCE_PROCESSOR_STARTED.compareAndSet(false, true)) {
            executor.execute(new ReferenceProcessor());
        }
    }

    private static final class ReferenceProcessor implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                final Reference<?> reference;
                try {
                    reference = REFERENCE_QUEUE.remove(100L);
                    if (reference != null) {
                        final WeakReferenceCleaner cleaner = (WeakReferenceCleaner) reference;
                        try {
                            cleaner.thunk.run();
                        } finally {
                            REFERENCE_MAP.remove(cleaner);
                        }
                    }
                } catch (InterruptedException e) {
                    LOGGER.warn("Interrupted while trying to retrieve reference, exiting.", e);
                    Thread.currentThread().interrupt();
                    return;
                } catch (RuntimeException e) {
                    LOGGER.warn("Exception while trying to process reference.", e);
                }
            }
        }
    }
}