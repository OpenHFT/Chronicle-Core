package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.shutdown.PriorityHook;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public enum BackgroundResourceReleaser {
    ; // none
    public static final String BACKGROUND_RESOURCE_RELEASER = "background~resource~releaser";
    static final boolean BG_RELEASER = Jvm.getBoolean("background.releaser", true);
    /**
     * Turn off the background thread if you want to manage the releasing in your own thread
     */
    private static final boolean BG_RELEASER_THREAD = BG_RELEASER && Jvm.getBoolean("background.releaser.thread", true);
    private static final BlockingQueue<Object> RESOURCES = new ArrayBlockingQueue<>(128);
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final Object POISON_PILL = new Object();
    private static final Thread RELEASER = BG_RELEASER_THREAD ? runBackgroundReleaserThread() : null;
    private static volatile boolean stopping = !BG_RELEASER;

    static {
        PriorityHook.add(99, BackgroundResourceReleaser::releasePendingResources);
    }

    private static Thread runBackgroundReleaserThread() {
        Thread thread = new Thread(BackgroundResourceReleaser::runReleaseResources, BACKGROUND_RESOURCE_RELEASER);
        thread.setDaemon(true);
        thread.start();

        return thread;
    }

    private static void runReleaseResources() {
        try {
            for (; ; ) {
                Object o = RESOURCES.take();
                if (o == POISON_PILL) {
                    Jvm.debug().on(BackgroundResourceReleaser.class, "Stopped thread");
                    break;
                }
                performRelease(o);
            }
        } catch (InterruptedException e) {
            // Restore the interrupt state...
            Thread.currentThread().interrupt();
            Jvm.warn().on(BackgroundResourceReleaser.class, "Died on interrupt");
        }
    }

    /**
     * Stops the background releasing thread after releasing pending resources.
     */
    public static void stop() {
        stopping = true;
        releasePendingResources();
        offerPoisonPill();
    }

    public static void release(AbstractCloseable closeable) {
        if (stopping)
            performRelease(closeable);
        else
            release0(closeable);
    }

    public static void release(AbstractReferenceCounted referenceCounted) {
        if (stopping)
            performRelease(referenceCounted);
        else
            release0(referenceCounted);
    }

    public static void run(Runnable runnable) {
        if (stopping)
            performRelease(runnable);
        else
            release0(runnable);
    }

    private static void release0(Object o) {
        COUNTER.incrementAndGet();
        if (RESOURCES.offer(o))
            return;
        performRelease(o);
    }

    public static void releasePendingResources() {
        try {
            for (; ; ) {
                Object o = RESOURCES.poll(1, TimeUnit.MILLISECONDS);
                if (o == null)
                    break;
                if (o != POISON_PILL)
                    performRelease(o);
            }
            if (stopping)
                offerPoisonPill();

            for (int i = 0; i < 1000 && COUNTER.get() > 0; i++)
                Jvm.pause(1);
            long left = COUNTER.get();
            if (left != 0)
                Jvm.perf().on(BackgroundResourceReleaser.class, "Still got " + left + " resources to clean");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void performRelease(Object o) {
        try {
            if (o instanceof AbstractCloseable)
                ((AbstractCloseable) o).callPerformClose();
            else if (o instanceof AbstractReferenceCounted)
                ((AbstractReferenceCounted) o).performRelease();
            else if (o instanceof Runnable)
                ((Runnable) o).run();
            else
                Jvm.warn().on(BackgroundResourceReleaser.class, "Don't know how to release a " + o.getClass());
        } catch (Throwable e) {
            Jvm.warn().on(BackgroundResourceReleaser.class, "Failed in release/close", e);
        } finally {
            COUNTER.decrementAndGet();
        }
    }

    /**
     * Is the current thread the background resource releaser thread?
     *
     * @return true if the current thread is the background resource releaser thread
     */
    public static boolean isOnBackgroundResourceReleaserThread() {
        return Thread.currentThread() == RELEASER;
    }

    private static void offerPoisonPill() {
        if (!RESOURCES.offer(POISON_PILL)) {
            Jvm.warn().on(BackgroundResourceReleaser.class, "Failed to add a stop object to the resource queue");
        }
    }

}
