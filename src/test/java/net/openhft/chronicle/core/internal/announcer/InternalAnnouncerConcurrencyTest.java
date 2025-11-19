/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.announcer;

import net.openhft.chronicle.core.announcer.Announcer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalAnnouncerConcurrencyTest {

    @BeforeAll
    static void disableOutput() {
        System.setProperty("chronicle.announcer.disable", "true");
    }

    @Test
    void concurrentAnnounceDoesNotRaceOrThrow() throws InterruptedException {
        int n = 8;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            final int idx = i;
            pool.execute(() -> {
                try {
                    start.await();
                    assertDoesNotThrow(() -> Announcer.announce("net.openhft", "artifact-" + (idx % 3), Collections.emptyMap()));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));
        pool.shutdownNow();
    }
}

