/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.scoped.ScopedResource;
import net.openhft.chronicle.core.scoped.ScopedResourcePool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StringBuilderPoolTest extends CoreTestCommon {

    @DisplayName("Create thread local provides pool string")
    @ParameterizedTest
    @ValueSource(ints = {-1, 2})
    void createThreadLocalProvidesPool(int capacity) {
        ScopedResourcePool<StringBuilder> pool =
                capacity < 0 ? StringBuilderPool.createThreadLocal()
                             : StringBuilderPool.createThreadLocal(capacity);
        assertNotNull(pool, "Thread-local StringBuilderPool should be created successfully");
    }

    @DisplayName("Reuses builder within thread and clears content")
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
            assertSame(firstBuilder, builder, "builder pool should return same instance (reference equality)");
            assertEquals(0, builder.length(), "Builder should be cleared before reuse");
        }
    }

    @DisplayName("Supplies independent builders per thread string")
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
        assertNotNull(otherThreadBuilder.get(), "Worker thread should have received a builder instance");
        assertNotSame(mainThreadBuilder.get(), otherThreadBuilder.get(), "Builders must be isolated per thread");

        try (ScopedResource<StringBuilder> resource = pool.get()) {
            assertSame(mainThreadBuilder.get(), resource.get(), "Main thread should regain its original builder");
        }
    }
}
