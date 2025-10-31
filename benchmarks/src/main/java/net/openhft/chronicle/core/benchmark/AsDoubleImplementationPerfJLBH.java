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
package net.openhft.chronicle.core;

import net.openhft.chronicle.jlbh.JLBH;
import net.openhft.chronicle.jlbh.JLBHOptions;
import net.openhft.chronicle.jlbh.JLBHTask;
import net.openhft.chronicle.jlbh.TeamCityHelper;

public class AsDoubleImplementationPerfJLBH implements JLBHTask {

    private static final int ITERATIONS = 100_000;
    private final long[] values = new long[]{21738192378192L, 12L, 13781678L, 124372189L};
    private final int[] exponents = new int[]{-50, -10, 0, 10, 50};
    private final boolean[] negative = new boolean[]{true, false};
    private final int[] decimalPlaces = new int[]{0, 10, 20, 50};
    private JLBH jlbh;

    public static void main(String[] args) {
        System.setProperty("jvm.resource.tracing", "false");
        new JLBH(new JLBHOptions()
                .jlbhTask(new AsDoubleImplementationPerfJLBH())
                .iterations(ITERATIONS)
                .warmUpIterations(10_000)
                .runs(3)
                .throughput(5_000)
                .recordOSJitter(false))
                .start();
    }

    @Override
    public void init(JLBH jlbh) {
        this.jlbh = jlbh;
    }

    @Override
    public void run(long startTimeNS) {
        for (int v = 0; v < values.length; v++) {
            for (int e = 0; e < exponents.length; e++) {
                for (int n = 0; n < negative.length; n++) {
                    for (int d = 0; d < decimalPlaces.length; d++) {
                        Maths.asDouble(values[v], exponents[e], negative[n], decimalPlaces[d]);
                    }
                }
            }
        }
        jlbh.sampleNanos(System.nanoTime() - startTimeNS);
    }

    @Override
    public void complete() {
        TeamCityHelper.teamCityStatsLastRun(getClass().getSimpleName(), jlbh, ITERATIONS, System.out);
    }
}
