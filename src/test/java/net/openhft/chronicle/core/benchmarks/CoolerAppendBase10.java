package net.openhft.chronicle.core.benchmarks;

import net.openhft.chronicle.core.cooler.CoolerTester;
import net.openhft.chronicle.core.cooler.CpuCoolers;
import net.openhft.chronicle.core.io.UnsafeText;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public class CoolerAppendBase10 {

    static long blackhole;

    public static void main(String[] args) {
        long address = UNSAFE.allocateMemory(32);
        new CoolerTester(CpuCoolers.PAUSE1, CpuCoolers.BUSY100)
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

}
