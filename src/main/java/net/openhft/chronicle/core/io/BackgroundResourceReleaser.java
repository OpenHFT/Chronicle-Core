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

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.shutdown.PriorityHook;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for managing reference-counted resources and related operations.
 * <p>
 * This class facilitates the background releasing of resources when they are no longer needed.
 * It uses a background thread to perform the release operations, which can be disabled if needed
 * using system properties.
 * </p>
 */
public final class BackgroundResourceReleaser {

    // Suppresses default constructor, ensuring non-instantiability.
    private BackgroundResourceReleaser() {
    }

    // Constant for background resource releaser thread name
    public static final String BACKGROUND_RESOURCE_RELEASER = "background~resource~releaser";

    // Flag to determine if background releasing is enabled
    static final boolean BG_RELEASER = Jvm.getBoolean("background.releaser", true);

    /**
     * Flag to determine if the background releaser thread is enabled.
     * If enabled, a background thread will be started for releasing resources.
     */
    private static final boolean BG_RELEASER_THREAD = BG_RELEASER && Jvm.getBoolean("background.releaser.thread", true);

    // Queue to hold resources pending release
    private static final BlockingQueue<Object> RESOURCES = new ArrayBlockingQueue<>(128);

    // Counter for tracking the number of pending release operations
    private static final AtomicLong COUNTER = new AtomicLong();

    // Special object used to signal the background thread to stop
    private static final Object POISON_PILL = new Object();

    // Background thread for releasing resources, if enabled
    private static final Thread RELEASER = BG_RELEASER_THREAD ? runBackgroundReleaserThread() : null;

    // Flag indicating if the background releaser is stopping
    private static volatile boolean stopping = !BG_RELEASER;

    static {
        // Add a hook to release pending resources during shutdown
        PriorityHook.add(99, BackgroundResourceReleaser::releasePendingResources);
    }

    /**
     * Starts the background thread for releasing resources.
     *
     * @return The background thread that was started.
     */
    private static Thread runBackgroundReleaserThread() {
        // Create and start the background thread for releasing resources
        Thread thread = new Thread(BackgroundResourceReleaser::runReleaseResources, BACKGROUND_RESOURCE_RELEASER);
        thread.setDaemon(true);
        thread.start();

        return thread;
    }

    /**
     * Main loop for the background resource releaser thread.
     * Continuously waits for resources to be added to the queue and releases them.
     */
    private static void runReleaseResources() {
        try {
            for (; ; ) {
                Object o = RESOURCES.take();  // Wait for a resource to be added to the queue
                if (o == POISON_PILL) {
                    Jvm.debug().on(BackgroundResourceReleaser.class, "Stopped thread");
                    break;
                }
                performRelease(o, true);  // Perform the release of the resource
            }
        } catch (InterruptedException e) {
            // Restore the interrupt state and log a warning
            Thread.currentThread().interrupt();
            Jvm.warn().on(BackgroundResourceReleaser.class, "Died on interrupt");
        }
    }

    /**
     * Stops the background releasing thread after releasing pending resources.
     * <p>
     * This method should be called during the shutdown process to release any resources
     * that have not been released yet.
     * </p>
     */
    public static void stop() {
        stopping = true;  // Set the stopping flag
        releasePendingResources();  // Release any pending resources
        offerPoisonPill(true);  // Signal the background thread to stop
    }

    /**
     * Releases the specified closeable resource.
     * <p>
     * If the background releaser is stopping, the release is performed immediately;
     * otherwise, the resource is queued for background release.
     * </p>
     *
     * @param closeable The resource to release.
     */
    public static void release(AbstractCloseable closeable) {
        if (stopping)
            performRelease(closeable, false);
        else
            release0(closeable);
    }

    /**
     * Releases the specified reference-counted resource.
     * <p>
     * If the background releaser is stopping, the release is performed immediately;
     * otherwise, the resource is queued for background release.
     * </p>
     *
     * @param referenceCounted The resource to release.
     */
    public static void release(AbstractReferenceCounted referenceCounted) {
        if (stopping)
            performRelease(referenceCounted, false);
        else
            release0(referenceCounted);
    }

    /**
     * Executes the specified runnable to release the resource.
     * <p>
     * If the background releaser is stopping, the runnable is executed immediately;
     * otherwise, it is queued for background execution.
     * </p>
     *
     * @param runnable The runnable to execute for releasing the resource.
     */
    public static void run(Runnable runnable) {
        if (stopping)
            performRelease(runnable, false);
        else
            release0(runnable);
    }

    /**
     * Queues the specified object for background release, or releases it immediately if the queue is full.
     *
     * @param o The object to release.
     */
    private static void release0(Object o) {
        COUNTER.incrementAndGet();  // Increment the pending release counter
        if (RESOURCES.offer(o))  // Attempt to add the object to the queue
            return;
        performRelease(o, true);  // If the queue is full, perform the release immediately
    }

    /**
     * Releases all pending resources.
     * <p>
     * This method should be called when you want to ensure that all the resources that
     * have been queued for release are actually released.
     * </p>
     */
    public static void releasePendingResources() {
        boolean interrupted = Thread.interrupted();  // Check if the thread was interrupted
        try {
            for (; ; ) {
                Object o = RESOURCES.poll(1, TimeUnit.MILLISECONDS);  // Poll for resources in the queue
                if (o == null)
                    break;
                if (o != POISON_PILL)
                    performRelease(o, true);
            }
            if (stopping)
                offerPoisonPill(false);

            if (!interrupted)
                for (int i = 0; i < 1000 && COUNTER.get() > 0; i++)
                    Thread.sleep(1);  // Wait for remaining resources to be released
            long left = COUNTER.get();
            if (left != 0)
                Jvm.perf().on(BackgroundResourceReleaser.class, "Still got " + left + " resources to clean");

        } catch (InterruptedException e) {
            Jvm.warn().on(BackgroundResourceReleaser.class, "Interrupted in releasePendingResources");
            interrupted = true;
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();  // Restore the interrupt state
        }
    }

    /**
     * Performs the release operation on the specified object.
     * <p>
     * This method checks the type of the object and performs the appropriate release operation.
     * </p>
     *
     * @param o       The object to release.
     * @param counted Whether to decrement the pending release counter after releasing.
     */
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
                COUNTER.decrementAndGet();  // Decrement the counter if needed
        }
    }

    /**
     * Checks if the current thread is the background resource releaser thread.
     *
     * @return True if the current thread is the background resource releaser thread; false otherwise.
     */
    public static boolean isOnBackgroundResourceReleaserThread() {
        return Thread.currentThread() == RELEASER;
    }

    /**
     * Adds a poison pill to the resource queue to signal the background thread to stop.
     * <p>
     * A poison pill is a special object that, when encountered by the background thread,
     * causes it to terminate.
     * </p>
     *
     * @param warn If true, a warning is logged if adding the poison pill fails.
     */
    private static void offerPoisonPill(boolean warn) {
        if (!RESOURCES.offer(POISON_PILL) && warn) {
            Jvm.warn().on(BackgroundResourceReleaser.class, "Failed to add a stop object to the resource queue");
        }
    }
}
