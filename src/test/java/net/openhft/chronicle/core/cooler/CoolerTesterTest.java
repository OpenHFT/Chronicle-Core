/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cooler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CoolerTesterTest {

    @Test
    @DisplayName("Should execute callable with each cooler")
    void shouldExecuteCallableWithEachCooler() throws Exception {
        Callable<?> mockTask = mock(Callable.class);
        CpuCooler mockCooler = mock(CpuCooler.class);

        CoolerTester tester = new CoolerTester(mockCooler, mockTask);
        tester.repeat(1).minCount(1).maxCount(1);

        tester.run();

        verify(mockTask, atLeastOnce()).call();
        verify(mockCooler, atLeastOnce()).disturb();
    }

    @Test
    @DisplayName("Getter methods should return correct values")
    void getterMethodsShouldReturnCorrectValues() {
        // Setup a CoolerTester instance with known configuration values
        CoolerTester tester = new CoolerTester();
        tester.repeat(5).runTimeMS(1000).minCount(10).maxCount(100);

        assertEquals(5, tester.repeat(), "repeat getter should return the configured repeat count");
        assertEquals(1000, tester.runTimeMS(), "runTimeMS getter should return the configured run time");
        assertEquals(10, tester.minCount(), "minCount getter should return the configured minimum count");
        assertEquals(100, tester.maxCount(), "maxCount getter should return the configured maximum count");
    }

    @Test
    @DisplayName("Run method should execute without errors cooler")
    void runMethodShouldExecuteWithoutErrors() {
        Callable<?> mockTask = mock(Callable.class);
        CpuCooler mockCooler = mock(CpuCooler.class);

        CoolerTester tester = new CoolerTester(mockCooler, mockTask);
        assertDoesNotThrow(tester::run, "CoolerTester.run should not throw with valid cooler and task");
    }
}
