package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public enum BackgroundResourceReleaser {
    ;
    private static final BlockingQueue<Object> RESOURCES = new ArrayBlockingQueue<>(128);
    private static final Thread RELEASER = new Thread(BackgroundResourceReleaser::runReleaseResources,
            "background-resource-releaser");

    static {
        RELEASER.setDaemon(true);
        RELEASER.start();
    }

    private static void runReleaseResources() {
        try {
            for (; ; ) {
                Object o = RESOURCES.take();
                performRelease(o);
            }
        } catch (InterruptedException e) {
            Jvm.warn().on(BackgroundResourceReleaser.class, "Died on interrupt");
        }
    }

    public static void release(AbstractCloseable closeable) {
        release0(closeable);
    }

    private static void release0(Object o) {
        if (RESOURCES.offer(o))
            return;
        performRelease(o);
    }

    public static void releasePendingResource() {
        for (; ; ) {
            Object o = RESOURCES.poll();
            if (o == null)
                return;
            performRelease(o);
        }
    }

    public static void release(AbstractReferenceCounted referenceCounted) {
        release0(referenceCounted);
    }

    private static void performRelease(Object o) {
        if (o instanceof AbstractCloseable)
            ((AbstractCloseable) o).performClose();
        else if (o instanceof AbstractReferenceCounted)
            ((AbstractReferenceCounted) o).performRelease();
        else
            Jvm.warn().on(BackgroundResourceReleaser.class, "Don't know how to release a " + o.getClass());
    }
}
