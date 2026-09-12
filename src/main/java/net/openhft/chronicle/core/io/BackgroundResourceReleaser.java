/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.shutdown.PriorityHook;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performs close and release operations on a background thread.
 * <p>
 * Closing in the background reduces worst case pause times because the caller
 * does not have to perform the tidy up work. The behaviour is controlled by three
 * system properties:
 * <ul>
 *   <li>{@code background.releaser} &mdash; set to {@code false} to disable
 *   queuing and release resources on the caller's thread.</li>
 *   <li>{@code background.releaser.thread} &mdash; set to {@code false} to queue
 *   work but require manual calls to {@link #releasePendingResources()}.</li>
 *   <li>{@code background.releaser.application.name} &mdash; an optional application
 *   label used in the releaser thread name.</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * MyCloseable closeable = new MyCloseable();
 * BackgroundResourceReleaser.release(closeable);
 * }</pre>
 */
public final class BackgroundResourceReleaser {

    // Suppresses default constructor, ensuring non-instantiability.
    private BackgroundResourceReleaser() {
    }

    public static final String BACKGROUND_RESOURCE_RELEASER = "background~resource~releaser";
    static final String APPLICATION_NAME_PROPERTY = "background.releaser.application.name";
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
        Thread thread = new Thread(BackgroundResourceReleaser::runReleaseResources, backgroundReleaserThreadName());
        thread.setDaemon(true);
        thread.start();

        return thread;
    }

    /**
     * Builds an attributable name for this class loader's releaser thread.
     * <p>
     * The application part is taken from {@code background.releaser.application.name}
     * when configured, otherwise from {@code sun.java.command}. A non-system defining
     * class loader is also identified because in-process launchers such as Maven
     * {@code exec:java} can load Chronicle Core more than once while sharing the same
     * Java command. The original bare name is retained when neither application nor
     * non-system class-loader context is available.
     *
     * @return the thread name, e.g. {@code com.example.MyApp/background~resource~releaser}
     */
    static String backgroundReleaserThreadName() {
        final String application = applicationContext();
        final String classLoader = classLoaderContext();
        if (application == null && classLoader == null)
            return BACKGROUND_RESOURCE_RELEASER;
        if (application == null)
            return classLoader + '/' + BACKGROUND_RESOURCE_RELEASER;
        if (classLoader == null)
            return application + '/' + BACKGROUND_RESOURCE_RELEASER;
        return application + '/' + classLoader + '/' + BACKGROUND_RESOURCE_RELEASER;
    }

    /**
     * Obtains the configured application label or derives the launch target from
     * {@code sun.java.command}.
     *
     * @return the configured label, the first whitespace-delimited command token,
     * or {@code null} when neither is available
     */
    private static String applicationContext() {
        try {
            final String configuredName = trimmedProperty(APPLICATION_NAME_PROPERTY);
            if (configuredName != null)
                return configuredName;

            String command = System.getProperty("sun.java.command");
            if (command == null)
                return null;
            command = command.trim();
            if (command.isEmpty())
                return null;
            for (int i = 0; i < command.length(); i++) {
                if (Character.isWhitespace(command.charAt(i)))
                    return command.substring(0, i);
            }
            return command;
        } catch (SecurityException e) {
            return null;
        }
    }

    private static String trimmedProperty(String propertyName) {
        final String value = System.getProperty(propertyName);
        if (value == null)
            return null;
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Identifies a non-system defining class loader so separate in-process
     * applications cannot receive identical releaser thread names.
     */
    private static String classLoaderContext() {
        final ClassLoader loader = BackgroundResourceReleaser.class.getClassLoader();
        if (loader == null)
            return "bootstrap";
        try {
            if (loader == ClassLoader.getSystemClassLoader())
                return null;
        } catch (SecurityException ignored) {
            // The defining loader is still safe to identify.
        }

        String type = loader.getClass().getSimpleName();
        if (type.isEmpty())
            type = loader.getClass().getName();
        return type + '@' + Integer.toHexString(System.identityHashCode(loader));
    }

    private static void runReleaseResources() {
        try {
            for (; ; ) {
                Object o = RESOURCES.take();
                if (o == POISON_PILL) {
                    Jvm.debug().on(BackgroundResourceReleaser.class, "Stopped thread");
                    break;
                }
                performRelease(o, true);
            }
        } catch (InterruptedException e) {
            // Restore the interrupt state...
            Thread.currentThread().interrupt();
            Jvm.warn().on(BackgroundResourceReleaser.class, "Died on interrupt");
        }
    }

    /**
     * Stops the background releasing thread after releasing pending resources.
     * <p>
     * It should be called during the shutdown process to release any resources that have not been
     * released yet.
     *
     */
    public static void stop() {
        stopping = true;
        releasePendingResources();
        offerPoisonPill(true);
    }

    /**
     * Releases the specified closeable resource.
     *
     * @param closeable the resource to release
     */
    public static void release(AbstractCloseable closeable) {
        if (stopping)
            performRelease(closeable, false);
        else
            release0(closeable);
    }

    /**
     * Releases the specified reference counted resource.
     *
     * @param referenceCounted the resource to release
     */
    public static void release(AbstractReferenceCounted referenceCounted) {
        if (stopping)
            performRelease(referenceCounted, false);
        else
            release0(referenceCounted);
    }

    /**
     * Executes the specified runnable to release the resource.
     *
     * @param runnable the runnable to execute for releasing the resource
     */
    public static void run(Runnable runnable) {
        if (stopping)
            performRelease(runnable, false);
        else
            release0(runnable);
    }

    private static void release0(Object o) {
        COUNTER.incrementAndGet();
        if (RESOURCES.offer(o))
            return;
        performRelease(o, true);
    }

    /**
     * Releases all pending resources.
     * <p>
     * Should be called when you want to make sure that all the resources that have been
     * queued for release are actually released.
     *
     */
    public static void releasePendingResources() {
        boolean interrupted = Thread.interrupted();
        try {
            for (; ; ) {
                Object o = RESOURCES.poll(1, TimeUnit.MILLISECONDS);
                if (o == null)
                    break;
                if (o != POISON_PILL)
                    performRelease(o, true);
            }
            if (stopping)
                offerPoisonPill(false);

            if (!interrupted)
                for (int i = 0; i < 1000 && COUNTER.get() > 0; i++) {
                    Jvm.pause(1);
                    if (Thread.currentThread().isInterrupted()) {
                        interrupted = true;
                        break;
                    }
                }
            long left = COUNTER.get();
            if (left != 0)
                Jvm.perf().on(BackgroundResourceReleaser.class, "Still got " + left + " resources to clean");

        } catch (InterruptedException e) {
            Jvm.warn().on(BackgroundResourceReleaser.class, "Interrupted in releasePendingResources");
            interrupted = true;
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    private static void performRelease(Object o, boolean counted) {
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
            if (counted)
                COUNTER.decrementAndGet();
        }
    }

    /**
     * Checks if the current thread is the background resource releaser thread.
     *
     * @return true if the current thread is the background resource releaser thread; false otherwise.
     */
    public static boolean isOnBackgroundResourceReleaserThread() {
        return Thread.currentThread() == RELEASER;
    }

    private static void offerPoisonPill(boolean warn) {
        if (!RESOURCES.offer(POISON_PILL) && warn) {
            Jvm.warn().on(BackgroundResourceReleaser.class, "Failed to add a stop object to the resource queue");
        }
    }
}
