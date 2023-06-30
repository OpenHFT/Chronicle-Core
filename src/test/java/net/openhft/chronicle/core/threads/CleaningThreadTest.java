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

package net.openhft.chronicle.core.threads;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

public class CleaningThreadTest extends CoreTestCommon {

    /**
     * Test whether CleaningThreadLocal is cleaned up properly.
     */
    @Test
    public void cleanupThreadLocal() throws InterruptedException {
        final String expectedThreadName = "ctl-test";
        BlockingQueue<String> threadNames = new LinkedBlockingQueue<>();

        CleaningThreadLocal<String> threadNameLocal = CleaningThreadLocal.withCleanup(
                () -> Thread.currentThread().getName(),
                threadNames::add
        );

        CleaningThread cleaningThread = new CleaningThread(
                () -> assertEquals("Thread name should match", expectedThreadName, threadNameLocal.get()),
                expectedThreadName
        );

        cleaningThread.start();

        String retrievedThreadName = threadNames.poll(1, TimeUnit.SECONDS);
        assertNotNull("Thread name should be retrieved", retrievedThreadName);
        assertEquals("Thread names should match", expectedThreadName, retrievedThreadName);
    }

    /**
     * Test CleaningThreadLocal remove functionality.
     */
    @Test
    public void testRemove() {
        int[] counter = {0};

        CleaningThreadLocal<Integer> counterLocal = CleaningThreadLocal.withCloseQuietly(() -> counter[0]++);

        assertEquals("Initial value should be 0", 0, (int) counterLocal.get());

        CleaningThread.performCleanup(Thread.currentThread());

        assertEquals("Value should be incremented after cleanup", 1, (int) counterLocal.get());
    }

    /**
     * Test whether CleaningThread resets thread affinity.
     */
    @Test
    public void resetThreadAffinity() throws InterruptedException {
        final BitSet initialAffinity = Affinity.getAffinity();

        // Only run the test if we have sufficient affinity cardinality
        assumeTrue("Affinity cardinality should be more than 2", initialAffinity.cardinality() > 2);
        assumeTrue("BASE_AFFINITY cardinality should be more than 2", AffinityLock.BASE_AFFINITY.cardinality() > 2);

        try {
            Affinity.setAffinity(1);

            BitSet[] nestedAffinity = {null};

            CleaningThread cleaningThread = new CleaningThread(() -> nestedAffinity[0] = Affinity.getAffinity());

            cleaningThread.start();
            cleaningThread.join();

            assertEquals("Nested affinity should match BASE_AFFINITY", AffinityLock.BASE_AFFINITY, nestedAffinity[0]);
        } finally {
            // Reset the affinity to what it was initially
            Affinity.setAffinity(initialAffinity);
        }
    }
}
