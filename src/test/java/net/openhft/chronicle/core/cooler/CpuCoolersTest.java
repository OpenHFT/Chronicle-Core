package net.openhft.chronicle.core.cooler;

import org.junit.jupiter.api.Test;

import static org.junit.Assume.assumeFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CpuCoolersTest {

    @Test
    public void testPark() {
        assertDoesNotThrow(CpuCoolers.PARK::disturb);
    }

    @Test
    public void testPause1() {
        assertDoesNotThrow(CpuCoolers.PAUSE1::disturb);
    }

    @Test
    public void testPause3() {
        assertDoesNotThrow(CpuCoolers.PAUSE3::disturb);
    }

    @Test
    public void testPause6() {
        assertDoesNotThrow(CpuCoolers.PAUSE6::disturb);
    }

    @Test
    public void testPause10() {
        assertDoesNotThrow(CpuCoolers.PAUSE10::disturb);
    }

    @Test
    public void testPause100() {
        assertDoesNotThrow(CpuCoolers.PAUSE100::disturb);
    }

    @Test
    public void testPause1000() {
        assertDoesNotThrow(CpuCoolers.PAUSE1000::disturb);
    }
    // ... additional tests for each enum constant

    @Test
    public void testYield() {
        assertDoesNotThrow(CpuCoolers.YIELD::disturb);
    }

    @Test
    public void testBUSY() {
        assertDoesNotThrow(CpuCoolers.BUSY::disturb);
    }

    @Test
    public void testBUSY_3() {
        assertDoesNotThrow(CpuCoolers.BUSY_3::disturb);
    }

    @Test
    public void testBUSY3() {
        assertDoesNotThrow(CpuCoolers.BUSY3::disturb);
    }

    @Test
    public void testBUSY10() {
        assertDoesNotThrow(CpuCoolers.BUSY10::disturb);
    }

    @Test
    public void testBUSY30() {
        assertDoesNotThrow(CpuCoolers.BUSY30::disturb);
    }

    @Test
    public void testBUSY100() {
        assertDoesNotThrow(CpuCoolers.BUSY100::disturb);
    }

    @Test
    public void testBUSY300() {
        assertDoesNotThrow(CpuCoolers.BUSY300::disturb);
    }

    @Test
    public void testBUSY1000() {
        assertDoesNotThrow(CpuCoolers.BUSY1000::disturb);
    }

    @Test
    public void testAffinity() {
        assumeFalse(Runtime.getRuntime().availableProcessors() < 2);
        assertDoesNotThrow(CpuCoolers.AFFINITY::disturb);
    }

    @Test
    public void testSerialization() {
        assertDoesNotThrow(CpuCoolers.SERIALIZATION::disturb);
    }

    @Test
    public void testMemoryCopy() {
        assertDoesNotThrow(CpuCoolers.MEMORY_COPY::disturb);
    }

    @Test
    public void testAll() {
        assertDoesNotThrow(CpuCoolers.ALL::disturb);
    }
}
