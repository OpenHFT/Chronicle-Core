/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cooler;

import net.openhft.chronicle.core.test.RecordingCallable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoolerTesterTest {

    @Test
    void shouldExecuteCallableWithEachCooler() throws Exception {
        RecordingCallable<?> task = new RecordingCallable<>();
        RecordingCpuCooler cooler = new RecordingCpuCooler();

        CoolerTester tester = new CoolerTester(cooler, task);
        tester.repeat(1).minCount(1).maxCount(1);

        tester.run();

        assertTrue(task.callCount() > 0);
        assertTrue(cooler.disturbCount > 0);
    }

    @Test
    void getterMethodsShouldReturnCorrectValues() {
        // Setup a CoolerTester instance with known configuration values
        CoolerTester tester = new CoolerTester();
        tester.repeat(5).runTimeMS(1000).minCount(10).maxCount(100);

        assertEquals(5, tester.repeat());
        assertEquals(1000, tester.runTimeMS());
        assertEquals(10, tester.minCount());
        assertEquals(100, tester.maxCount());
    }

    @Test
    void runMethodShouldExecuteWithoutErrors() {
        RecordingCallable<?> task = new RecordingCallable<>();
        RecordingCpuCooler cooler = new RecordingCpuCooler();

        CoolerTester tester = new CoolerTester(cooler, task);
        assertDoesNotThrow(tester::run);
    }

    private static final class RecordingCpuCooler implements CpuCooler {
        private int disturbCount;

        @Override
        public void disturb() {
            disturbCount++;
        }
    }
}
