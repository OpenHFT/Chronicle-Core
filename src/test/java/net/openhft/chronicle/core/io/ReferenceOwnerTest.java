/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceOwnerTest {
    @Test
    @DisplayName("Reference owner ids are unique across instances")
    void testReferenceId() {
        Set<Integer> ints = new HashSet<>();
        for (int i = 0; i < 101; i++)
            ints.add(new VanillaReferenceOwner("hi").referenceId());
        assertEquals(100, ints.size(), 1, "Reference IDs should be unique across 101 instances within delta of 1");
    }
}
