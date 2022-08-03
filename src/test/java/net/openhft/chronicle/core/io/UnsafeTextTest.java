/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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