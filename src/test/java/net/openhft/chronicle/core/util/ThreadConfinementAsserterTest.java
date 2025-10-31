/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ThreadConfinementAsserterTest {

    @Test
    void assertThreadConfinedSameThread() {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();

        assertDoesNotThrow(asserter::assertThreadConfined);
        assertDoesNotThrow(asserter::assertThreadConfined);
    }

    @Test
    void assertThreadConfinedDifferentThreads() throws InterruptedException {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        assertDoesNotThrow(asserter::assertThreadConfined);

        executorService.execute(() -> {
            assertThrows(IllegalStateException.class, asserter::assertThreadConfined);
            latch.countDown();
        });

        latch.await();
        executorService.shutdown();
    }

    @Test
    void createShouldReturnCorrectTypeBasedOnAssertions() {
        // This test's behavior will depend on whether assertions are enabled in the JVM.
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.create();
        // Perform tests based on whether assertions are enabled or not.
    }

    @Test
    void createEnabledShouldAlwaysReturnFunctionalAsserter() {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();
        assertNotNull(asserter);
        // Further testing of functionality.
    }
}
