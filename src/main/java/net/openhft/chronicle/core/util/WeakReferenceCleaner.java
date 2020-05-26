/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Supplier;

/**
 * Intended as a replacement for usage of sun.misc.Cleaner/jdk.internal.ref.Cleaner
 */
public final class WeakReferenceCleaner extends WeakReference<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeakReferenceCleaner.class);
    private static final String THREAD_NAME = "chronicle-weak-reference-cleaner";
    private static final ReferenceQueue<Object> REFERENCE_QUEUE = new ReferenceQueue<>();
    private static final ConcurrentLinkedQueue<WeakReferenceCleaner> SCHEDULED_CLEAN = new ConcurrentLinkedQueue<>();
    /*
     * This set is used to hold a STRONG reference to the WeakReferenceCleaner object, to avoid it being collected
     * before GC added it to the reference queue
     */
    private static final Set<WeakReferenceCleaner> REFERENCE_SET = Collections.synchronizedSet(new HashSet<>(128));
    private static final AtomicBoolean REFERENCE_PROCESSOR_STARTED = new AtomicBoolean(false);
    private static final AtomicIntegerFieldUpdater<WeakReferenceCleaner> CLEANED_FLAG =
            AtomicIntegerFieldUpdater.newUpdater(WeakReferenceCleaner.class, "cleaned");
    static final AtomicBoolean THREAD_SHUTTING_DOWN = new AtomicBoolean();

    private final Runnable thunk;
    @SuppressWarnings("unused")
    private volatile int cleaned = 0;

    private WeakReferenceCleaner(final Object referent, final Runnable thunk) {
        super(referent, REFERENCE_QUEUE);
        this.thunk = thunk;
    }

    public static WeakReferenceCleaner newCleaner(final Object referent, final Runnable thunk) {
        startCleanerThreadIfNotStarted();

        final WeakReferenceCleaner cleaner = new WeakReferenceCleaner(referent, thunk);
        REFERENCE_SET.add(cleaner);
        return cleaner;
    }

    static void startCleanerThreadIfNotStarted() {
        if (REFERENCE_PROCESSOR_STARTED.compareAndSet(false, true)) {
            final Thread thread = new Thread(new ReferenceProcessor(), THREAD_NAME);
            thread.setDaemon(true);
            thread.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> THREAD_SHUTTING_DOWN.set(true), THREAD_NAME+"-shutdown-hook"));
        }
    }

    /**
     * Runs the cleaner associated with the referent.
     */
    public void clean() {
        if (CLEANED_FLAG.compareAndSet(this, 0, 1))
            thunk.run();
    }

    /**
     * Schedules this WeakReferenceCleaner for cleaning
     * regardless if the associated referent is referenced or not.
     * <p>
     * Cleaning is performed by another thread at some unspecified
     * later time.
     */
    public void scheduleForClean() {
        SCHEDULED_CLEAN.add(this);
        REFERENCE_SET.remove(this);
    }

    private static final class ReferenceProcessor implements Runnable {

        public static final long TIMEOUT_MS = 50L; // 20 Hz

        @Override
        public void run() {
            final Thread thread = Thread.currentThread();
            while (!(THREAD_SHUTTING_DOWN.get() || thread.isInterrupted())) {
                try {
                    // prioritise scheduled cleaners
                    WeakReferenceCleaner wrc;
                    while ((wrc = SCHEDULED_CLEAN.poll()) != null)
                        wrc.clean();

                    Reference<?> reference;
                    while ((reference = REFERENCE_QUEUE.remove(TIMEOUT_MS)) != null) {
                        final WeakReferenceCleaner cleaner = (WeakReferenceCleaner) reference;
                        REFERENCE_SET.remove(cleaner);
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
            LOGGER.debug("Shut down, exiting.");
        }
    }
}