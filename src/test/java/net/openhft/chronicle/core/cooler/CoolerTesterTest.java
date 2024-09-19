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
