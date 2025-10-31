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
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.scoped.ScopedResource;
import net.openhft.chronicle.core.scoped.ScopedResourcePool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StringBuilderPoolTest extends CoreTestCommon {

    @ParameterizedTest
    @ValueSource(ints = {-1, 2})
    void createThreadLocalProvidesPool(int capacity) {
        ScopedResourcePool<StringBuilder> pool =
                capacity < 0 ? StringBuilderPool.createThreadLocal()
                             : StringBuilderPool.createThreadLocal(capacity);
        assertNotNull(pool);
    }

    @Test
    void reusesBuilderWithinThreadAndClearsContent() {
        ScopedResourcePool<StringBuilder> pool = StringBuilderPool.createThreadLocal(1);

        StringBuilder firstBuilder;
        try (ScopedResource<StringBuilder> resource = pool.get()) {
            StringBuilder builder = resource.get();
            builder.append("Chronicle");
            firstBuilder = builder;
        }

        try (ScopedResource<StringBuilder> resource = pool.get()) {
            StringBuilder builder = resource.get();
            assertSame(firstBuilder, builder);
            assertEquals(0, builder.length(), "Builder should be cleared before reuse");
        }
    }

    @Test
    void suppliesIndependentBuildersPerThread() throws InterruptedException {
        ScopedResourcePool<StringBuilder> pool = StringBuilderPool.createThreadLocal(1);

        AtomicReference<StringBuilder> mainThreadBuilder = new AtomicReference<>();
        try (ScopedResource<StringBuilder> resource = pool.get()) {
            mainThreadBuilder.set(resource.get());
        }

        CountDownLatch complete = new CountDownLatch(1);
        AtomicReference<StringBuilder> otherThreadBuilder = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try (ScopedResource<StringBuilder> resource = pool.get()) {
                otherThreadBuilder.set(resource.get());
            } finally {
                complete.countDown();
            }
        });
        worker.start();

        assertTrue(complete.await(5, TimeUnit.SECONDS), "Worker thread did not finish in time");
        worker.join(1000L);
        assertNotNull(otherThreadBuilder.get());
        assertNotSame(mainThreadBuilder.get(), otherThreadBuilder.get(), "Builders must be isolated per thread");

        try (ScopedResource<StringBuilder> resource = pool.get()) {
            assertSame(mainThreadBuilder.get(), resource.get(), "Main thread should regain its original builder");
        }
    }
}
