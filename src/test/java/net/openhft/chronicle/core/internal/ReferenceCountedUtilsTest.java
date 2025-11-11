/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.AbstractReferenceCounted;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReferenceCountedUtilsTest {

    @BeforeEach
    void setUp() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    @AfterEach
    void tearDown() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    @Test
    void unmonitorShouldRemoveReference() {
        AbstractReferenceCounted referenceCounted = mock(AbstractReferenceCounted.class);
        when(referenceCounted.refCount()).thenReturn(1);

        ReferenceCountedUtils.add(referenceCounted);
        ReferenceCountedUtils.unmonitor(referenceCounted);

        assertDoesNotThrow(ReferenceCountedUtils::assertReferencesReleased, "Unmonitored references should not be checked");
    }
}
