package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.cooler.CoolerTester;
import net.openhft.chronicle.core.cooler.CpuCoolers;
import org.junit.Test;

import java.util.stream.LongStream;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.Assert.assertEquals;

public class UnsafeTextTest {

    static long blackhole;

    @Test
    public void coolerAppendBase10quick() {
        long address = UNSAFE.allocateMemory(32);

        try {

            new CoolerTester(CpuCoolers.PAUSE1, CpuCoolers.BUSY1)
//                .add("noop", () -> null)
                    .add("20d", () -> {
                        blackhole = UnsafeText.appendFixed(address, -Integer.MAX_VALUE);
                        return null;
                    })
                    .runTimeMS(100)
                    .repeat(3)
                    .run();

            final String memVal = LongStream.range(address, blackhole)
                    .mapToInt(addr -> UNSAFE.getByte(null, addr))
                    .mapToObj(c -> (char) c)
                    .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                    .toString();

            assertEquals(Long.toString(-Integer.MAX_VALUE), memVal);
        } finally {
            UNSAFE.freeMemory(address);
        }

    }
}