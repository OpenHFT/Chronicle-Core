/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cooler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class CpuCoolersTest {

    @Test
    @DisplayName("Cpu cooler PARK disturb does not throw")
    void testPark() {
        assertDoesNotThrow(CpuCoolers.PARK::disturb, "Cpu cooler PARK disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler PAUSE1 disturb does not throw")
    void testPause1() {
        assertDoesNotThrow(CpuCoolers.PAUSE1::disturb, "Cpu cooler PAUSE1 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler PAUSE3 disturb does not throw")
    void testPause3() {
        assertDoesNotThrow(CpuCoolers.PAUSE3::disturb, "Cpu cooler PAUSE3 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler PAUSE6 disturb does not throw")
    void testPause6() {
        assertDoesNotThrow(CpuCoolers.PAUSE6::disturb, "Cpu cooler PAUSE6 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler PAUSE10 disturb does not throw")
    void testPause10() {
        assertDoesNotThrow(CpuCoolers.PAUSE10::disturb, "Cpu cooler PAUSE10 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler PAUSE100 disturb does not throw")
    void testPause100() {
        assertDoesNotThrow(CpuCoolers.PAUSE100::disturb, "Cpu cooler PAUSE100 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler PAUSE1000 disturb does not throw")
    void testPause1000() {
        assertDoesNotThrow(CpuCoolers.PAUSE1000::disturb, "Cpu cooler PAUSE1000 disturb should not throw");
    }
    // ... additional tests for each enum constant

    @Test
    @DisplayName("Cpu cooler YIELD disturb does not throw")
    void testYield() {
        assertDoesNotThrow(CpuCoolers.YIELD::disturb, "Cpu cooler YIELD disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler BUSY disturb does not throw")
    void testBUSY() {
        assertDoesNotThrow(CpuCoolers.BUSY::disturb, "Cpu cooler BUSY disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler BUSY_3 disturb does not throw")
    void testBUSY_3() {
        assertDoesNotThrow(CpuCoolers.BUSY_3::disturb, "Cpu cooler BUSY_3 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler BUSY3 disturb does not throw")
    void testBUSY3() {
        assertDoesNotThrow(CpuCoolers.BUSY3::disturb, "Cpu cooler BUSY3 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler BUSY10 disturb does not throw")
    void testBUSY10() {
        assertDoesNotThrow(CpuCoolers.BUSY10::disturb, "Cpu cooler BUSY10 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler BUSY30 disturb does not throw")
    void testBUSY30() {
        assertDoesNotThrow(CpuCoolers.BUSY30::disturb, "Cpu cooler BUSY30 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler BUSY100 disturb does not throw")
    void testBUSY100() {
        assertDoesNotThrow(CpuCoolers.BUSY100::disturb, "Cpu cooler BUSY100 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler BUSY300 disturb does not throw")
    void testBUSY300() {
        assertDoesNotThrow(CpuCoolers.BUSY300::disturb, "Cpu cooler BUSY300 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler BUSY1000 disturb does not throw")
    void testBUSY1000() {
        assertDoesNotThrow(CpuCoolers.BUSY1000::disturb, "Cpu cooler BUSY1000 disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler AFFINITY disturb does not throw")
    void testAffinity() {
        assumeFalse(Runtime.getRuntime().availableProcessors() < 2, "affinity cooler needs at least two processors");
        assertDoesNotThrow(CpuCoolers.AFFINITY::disturb, "Cpu cooler AFFINITY disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler SERIALIZATION disturb does not throw")
    void testSerialization() {
        assertDoesNotThrow(CpuCoolers.SERIALIZATION::disturb, "Cpu cooler SERIALIZATION disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler MEMORY_COPY disturb does not throw")
    void testMemoryCopy() {
        assertDoesNotThrow(CpuCoolers.MEMORY_COPY::disturb, "Cpu cooler MEMORY_COPY disturb should not throw");
    }

    @Test
    @DisplayName("Cpu cooler ALL disturb does not throw")
    void testAll() {
        assertDoesNotThrow(CpuCoolers.ALL::disturb, "Cpu cooler ALL disturb should not throw");
    }
}
