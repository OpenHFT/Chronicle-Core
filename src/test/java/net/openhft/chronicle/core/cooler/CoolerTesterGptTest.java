package net.openhft.chronicle.core.cooler;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertTrue;

public class CoolerTesterGptTest {

    private CoolerTester coolerTester;

    @Before
    public void setup() {
        Callable<Void> task = () -> {
            // some task that doesn't return anything
            return null;
        };
        coolerTester = new CoolerTester(task, CpuCoolers.PAUSE1, CpuCoolers.PAUSE3);
    }

    @Test
    public void testAdd() {
        Callable<Void> additionalTask = () -> {
            // another task that doesn't return anything
            return null;
        };
        coolerTester.add("Test Task", additionalTask)
                .runTimeMS(100)
                .repeat(3)
                .run();
    }

    @Test
    public void testRepeat() {
        int repeat = coolerTester.repeat();
        assertTrue("Default repeat value should be 10", repeat == 10);
    }
}
