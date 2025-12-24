/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cooler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class CpuCoolersTest {

    @Test
    void testPark() {
        assertDoesNotThrow(CpuCoolers.PARK::disturb, "PARK cooler should not throw");
    }

    @Test
    void testPause1() {
        assertDoesNotThrow(CpuCoolers.PAUSE1::disturb, "PAUSE1 cooler should not throw");
    }

    @Test
    void testPause3() {
        assertDoesNotThrow(CpuCoolers.PAUSE3::disturb, "PAUSE3 cooler should not throw");
    }

    @Test
    void testPause6() {
        assertDoesNotThrow(CpuCoolers.PAUSE6::disturb, "PAUSE6 cooler should not throw");
    }

    @Test
    void testPause10() {
        assertDoesNotThrow(CpuCoolers.PAUSE10::disturb, "PAUSE10 cooler should not throw");
    }

    @Test
    void testPause100() {
        assertDoesNotThrow(CpuCoolers.PAUSE100::disturb, "PAUSE100 cooler should not throw");
    }

    @Test
    void testPause1000() {
        assertDoesNotThrow(CpuCoolers.PAUSE1000::disturb, "PAUSE1000 cooler should not throw");
    }
    // ... additional tests for each enum constant

    @Test
    void testYield() {
        assertDoesNotThrow(CpuCoolers.YIELD::disturb, "YIELD cooler should not throw");
    }

    @Test
    void testBUSY() {
        assertDoesNotThrow(CpuCoolers.BUSY::disturb, "BUSY cooler should not throw");
    }

    @Test
    void testBUSY_3() {
        assertDoesNotThrow(CpuCoolers.BUSY_3::disturb, "BUSY_3 cooler should not throw");
    }

    @Test
    void testBUSY3() {
        assertDoesNotThrow(CpuCoolers.BUSY3::disturb, "BUSY3 cooler should not throw");
    }

    @Test
    void testBUSY10() {
        assertDoesNotThrow(CpuCoolers.BUSY10::disturb, "BUSY10 cooler should not throw");
    }

    @Test
    void testBUSY30() {
        assertDoesNotThrow(CpuCoolers.BUSY30::disturb, "BUSY30 cooler should not throw");
    }

    @Test
    void testBUSY100() {
        assertDoesNotThrow(CpuCoolers.BUSY100::disturb, "BUSY100 cooler should not throw");
    }

    @Test
    void testBUSY300() {
        assertDoesNotThrow(CpuCoolers.BUSY300::disturb, "BUSY300 cooler should not throw");
    }

    @Test
    void testBUSY1000() {
        assertDoesNotThrow(CpuCoolers.BUSY1000::disturb, "BUSY1000 cooler should not throw");
    }

    @Test
    void testAffinity() {
        assumeFalse(Runtime.getRuntime().availableProcessors() < 2);
        assertDoesNotThrow(CpuCoolers.AFFINITY::disturb, "AFFINITY cooler should not throw");
    }

    @Test
    void testSerialization() {
        assertDoesNotThrow(CpuCoolers.SERIALIZATION::disturb, "SERIALIZATION cooler should not throw");
    }

    @Test
    void testMemoryCopy() {
        assertDoesNotThrow(CpuCoolers.MEMORY_COPY::disturb, "MEMORY_COPY cooler should not throw");
    }

    @Test
    void testAll() {
        assertDoesNotThrow(CpuCoolers.ALL::disturb, "ALL cooler should not throw");
    }
}
