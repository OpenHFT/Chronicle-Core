/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

class ReferenceOwnerTest {
    @Test
    void testReferenceId() {
        Set<Integer> ints = new HashSet<>();
        for (int i = 0; i < 101; i++)
            ints.add(new VanillaReferenceOwner("hi").referenceId());
        assertEquals(100.0, (double) ints.size(), 1.0);
    }
}
