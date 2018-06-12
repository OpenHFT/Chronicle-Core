package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Supplier;

/**
 * Intended as a replacement for usage of sun.misc.Cleaner/jdk.internal.ref.Cleaner
 */
public final class WeakReferenceCleaner extends WeakReference<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeakReferenceCleaner.class);
    private static final ReferenceQueue<Object> REFERENCE_QUEUE = new ReferenceQueue<>();
    private static final ConcurrentLinkedQueue<WeakReferenceCleaner> SCHEDULED_CLEAN = new ConcurrentLinkedQueue<>();
    private static final Map<WeakReferenceCleaner, Boolean> REFERENCE_MAP =
            Collections.synchronizedMap(
                    new HashMap<>(128));
    private static final AtomicBoolean REFERENCE_PROCESSOR_STARTED = new AtomicBoolean(false);
    private static final AtomicIntegerFieldUpdater<WeakReferenceCleaner> CLEANED_FLAG =
            AtomicIntegerFieldUpdater.newUpdater(WeakReferenceCleaner.class, "cleaned");

    private final Runnable thunk;
    @SuppressWarnings("unused")
    private volatile int cleaned = 0;

    private WeakReferenceCleaner(final Object referent, final Runnable thunk) {
        super(referent, REFERENCE_QUEUE);
        this.thunk = thunk;
    }

    public static WeakReferenceCleaner newCleaner(final Object referent, final Runnable thunk) {
        startReferenceProcessor(WeakReferenceCleaner::referenceCleanerExecutor);

        final WeakReferenceCleaner cleaner = new WeakReferenceCleaner(referent, thunk);
        REFERENCE_MAP.put(cleaner, Boolean.TRUE);
        return cleaner;
    }

    public static void startReferenceProcessor(final Supplier<Executor> executorSupplier) {
        if (!REFERENCE_PROCESSOR_STARTED.get()) {
            if (REFERENCE_PROCESSOR_STARTED.compareAndSet(false, true)) {
                executorSupplier.get().execute(new ReferenceProcessor());
            }
        }
    }

    static Executor referenceCleanerExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            final Thread t = new Thread(r);
            t.setName("chronicle-weak-reference-cleaner");
            t.setDaemon(true);
            return t;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));

        return executor;
    }

    public static int referenceCount() {
        return REFERENCE_MAP.size();
    }

    public void clean() {
        if (CLEANED_FLAG.compareAndSet(this, 0, 1)) {
            thunk.run();
        }
    }

    public void scheduleForClean() {
        SCHEDULED_CLEAN.add(this);
        REFERENCE_MAP.remove(this);
    }

    static final class ReferenceProcessor implements Runnable {
        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            while (!thread.isInterrupted()) {
                try {
                    // prioritise scheduled cleaners
                    WeakReferenceCleaner wrc;
                    while ((wrc = SCHEDULED_CLEAN.poll()) != null) {
                        wrc.clean();
                    }

                    Reference<?> reference;
                    while ((reference = REFERENCE_QUEUE.remove(50L)) != null) {
                        final WeakReferenceCleaner cleaner = (WeakReferenceCleaner) reference;

                        cleaner.clean();
                    }

                } catch (InterruptedException e) {
                    LOGGER.debug("Interrupted while trying to retrieve reference, exiting.", e);
                    thread.interrupt();
                    return;
                } catch (Throwable e) {
                    LOGGER.warn("Exception while trying to process reference.", e);
                }
            }
        }
    }
}
