package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.cooler.CoolerTester;
import net.openhft.chronicle.core.cooler.CpuCooler;
import net.openhft.chronicle.core.cooler.CpuCoolers;
import org.junit.Test;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public class RawTextTest {

static long blackhole;
    @Test
    public void coolerAppendBase10() {
        long address = UNSAFE.allocateMemory(32);
        new CoolerTester(new CpuCooler[]{
                CpuCoolers.PAUSE1,
                CpuCoolers.BUSY100
        })
//                .add("noop", () -> null)
                .add("coolerAppendBase10 (long)", () -> {
                    blackhole = RawText.appendBase10(address, (long) Integer.MAX_VALUE);
                    return null;
                })
                .runTimeMS(10000)
                .repeat(6)
                .run();

        UNSAFE.freeMemory(address);
    }
}