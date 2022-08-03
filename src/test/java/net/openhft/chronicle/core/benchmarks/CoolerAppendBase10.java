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
