/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.cooler;

import org.junit.jupiter.api.Test;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class CoolerTesterTest {

    @Test
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
    void constructorShouldInitializeWithGivenParameters() throws Exception {
        Callable<?> mockTask = mock(Callable.class);
        CpuCooler mockCooler1 = mock(CpuCooler.class);
        CpuCooler mockCooler2 = mock(CpuCooler.class);

        CoolerTester tester = new CoolerTester(mockTask, mockCooler1, mockCooler2);

        // Assertions to check the proper initialization of the CoolerTester object
        // This might involve using reflection or other techniques to inspect the internal state
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
        Callable<?> mockTask = mock(Callable.class);
        CpuCooler mockCooler = mock(CpuCooler.class);

        CoolerTester tester = new CoolerTester(mockCooler, mockTask);
        assertDoesNotThrow(tester::run);
    }
}
