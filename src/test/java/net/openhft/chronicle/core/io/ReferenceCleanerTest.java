/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceCleanerTest extends CoreTestCommon {

    @Test
    void cleanupRunsAfterReferentBecomesUnreachable() throws InterruptedException {
        final CountDownLatch cleaned = new CountDownLatch(1);
        Object referent = new Object();
        // NOTE: the action must not capture 'referent'.
        ReferenceCleaner.register(referent, cleaned::countDown);

        // Drop the only strong reference and encourage collection.
        referent = null;
        for (int i = 0; i < 50 && cleaned.getCount() > 0; i++) {
            System.gc();
            if (cleaned.await(100, TimeUnit.MILLISECONDS))
                break;
        }
        assertEquals(0, cleaned.getCount(), "cleanup action should have run once the referent was collected");
    }

    @Test
    void explicitCleanRunsActionExactlyOnce() {
        final AtomicInteger runs = new AtomicInteger();
        final Object referent = new Object();
        final ReferenceCleaner.Cleanable cleanable =
                ReferenceCleaner.register(referent, runs::incrementAndGet);

        cleanable.clean();
        cleanable.clean(); // idempotent

        assertEquals(1, runs.get());
        // referent kept reachable to this point so only the explicit path runs.
        assertNotNull(referent);
    }

    @Test
    void nullArgumentsRejected() {
        assertThrows(NullPointerException.class, () -> ReferenceCleaner.register(null, () -> {
        }));
        assertThrows(NullPointerException.class, () -> ReferenceCleaner.register(new Object(), null));
    }
}
