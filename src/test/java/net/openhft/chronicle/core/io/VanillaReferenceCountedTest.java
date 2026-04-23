/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VanillaReferenceCountedTest extends MonitorReferenceCountedContractTest {

    private AtomicInteger onReleasedCallCount;

    @BeforeEach
    void setUp() {
        onReleasedCallCount = new AtomicInteger(0);
    }

    @Override
    protected VanillaReferenceCounted createReferenceCounted() {
        return new VanillaReferenceCounted(onReleasedCallCount::incrementAndGet, VanillaReferenceCounted.class);
    }

    @Test
    void createdHereWillReturnNull() {
        final VanillaReferenceCounted referenceCounted = createReferenceCounted();
        assertNull(referenceCounted.createdHere());
    }
}
