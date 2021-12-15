package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.cooler.CoolerTester;
import net.openhft.chronicle.core.cooler.CpuCoolers;
import org.junit.Test;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class UnsafeTextTest {

    static long blackhole;

    @Test
    public void coolerAppendBase10quick() {
        long address = UNSAFE.allocateMemory(32);

        try {
            assertDoesNotThrow(() -> {
                new CoolerTester(CpuCoolers.PAUSE1, CpuCoolers.BUSY1)
//                .add("noop", () -> null)
                        .add("20d", () -> {
                            blackhole = UnsafeText.appendFixed(address, -Integer.MAX_VALUE);
                            return null;
                        })
                        .runTimeMS(100)
                        .repeat(3)
                        .run();
            });
        } finally {
            UNSAFE.freeMemory(address);
        }

    }
}