package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.cooler.CoolerTester;
import net.openhft.chronicle.core.cooler.CpuCooler;
import net.openhft.chronicle.core.cooler.CpuCoolers;
import org.junit.Ignore;
import org.junit.Test;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public class UnsafeTextTest {

    static long blackhole;

    @Test
    @Ignore("Performance test")
    public void coolerAppendBase10() {
        long address = UNSAFE.allocateMemory(32);
        new CoolerTester(new CpuCooler[]{
                CpuCoolers.PAUSE1,
                CpuCoolers.BUSY100
        })
//                .add("noop", () -> null)
                .add("20d", () -> {
                    blackhole = UnsafeText.appendFixed(address, -Integer.MAX_VALUE);
                    return null;
                })
                .runTimeMS(10000)
                .repeat(6)
                .run();

        UNSAFE.freeMemory(address);
    }

    @Test
    public void coolerAppendBase10quick() {
        long address = UNSAFE.allocateMemory(32);
        new CoolerTester(new CpuCooler[]{
                CpuCoolers.PAUSE1,
                CpuCoolers.BUSY1
        })
//                .add("noop", () -> null)
                .add("20d", () -> {
                    blackhole = UnsafeText.appendFixed(address, -Integer.MAX_VALUE);
                    return null;
                })
                .runTimeMS(100)
                .repeat(3)
                .run();

        UNSAFE.freeMemory(address);
    }
}