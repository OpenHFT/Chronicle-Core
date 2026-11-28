/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cooler;

import net.openhft.affinity.Affinity;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CpuCoolersTest {

    @Test
    void testPark() {
        assertDoesNotThrow(CpuCoolers.PARK::disturb);
    }

    @Test
    void testPause1() {
        assertDoesNotThrow(CpuCoolers.PAUSE1::disturb);
    }

    @Test
    void testPause3() {
        assertDoesNotThrow(CpuCoolers.PAUSE3::disturb);
    }

    @Test
    void testPause6() {
        assertDoesNotThrow(CpuCoolers.PAUSE6::disturb);
    }

    @Test
    void testPause10() {
        assertDoesNotThrow(CpuCoolers.PAUSE10::disturb);
    }

    @Test
    void testPause100() {
        assertDoesNotThrow(CpuCoolers.PAUSE100::disturb);
    }

    @Test
    void testPause1000() {
        assertDoesNotThrow(CpuCoolers.PAUSE1000::disturb);
    }
    // ... additional tests for each enum constant

    @Test
    void testYield() {
        assertDoesNotThrow(CpuCoolers.YIELD::disturb);
    }

    @Test
    void testBUSY() {
        assertDoesNotThrow(CpuCoolers.BUSY::disturb);
    }

    @Test
    void testBUSY_3() {
        assertDoesNotThrow(CpuCoolers.BUSY_3::disturb);
    }

    @Test
    void testBUSY3() {
        assertDoesNotThrow(CpuCoolers.BUSY3::disturb);
    }

    @Test
    void testBUSY10() {
        assertDoesNotThrow(CpuCoolers.BUSY10::disturb);
    }

    @Test
    void testBUSY30() {
        assertDoesNotThrow(CpuCoolers.BUSY30::disturb);
    }

    @Test
    void testBUSY100() {
        assertDoesNotThrow(CpuCoolers.BUSY100::disturb);
    }

    @Test
    void testBUSY300() {
        assertDoesNotThrow(CpuCoolers.BUSY300::disturb);
    }

    @Test
    void testBUSY1000() {
        assertDoesNotThrow(CpuCoolers.BUSY1000::disturb);
    }

    @Test
    void testAffinity() {
        assumeFalse(Runtime.getRuntime().availableProcessors() < 2);
        withRestoredAffinity(() -> assertDoesNotThrow(CpuCoolers.AFFINITY::disturb));
    }

    // A reused test fork may initialise AffinityLock after this fixture has run.
    // Do not let it capture the cooler's single-CPU mask as the process baseline.
    static void withRestoredAffinity(Runnable body) {
        BitSet original = (BitSet) Affinity.getAffinity().clone();
        try {
            body.run();
        } finally {
            Affinity.setAffinity(original);
        }
    }

    @Test
    void testSerialization() {
        assertDoesNotThrow(CpuCoolers.SERIALIZATION::disturb);
    }

    @Test
    void testMemoryCopy() {
        assertDoesNotThrow(CpuCoolers.MEMORY_COPY::disturb);
    }

    @Test
    void testAll() {
        assertDoesNotThrow(CpuCoolers.ALL::disturb);
    }
}
