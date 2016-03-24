/*
 *
 *  *     Copyright (C) 2016  higherfrequencytrading.com
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU Lesser General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU Lesser General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU Lesser General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.openhft.chronicle.core.latencybenchmark;

import net.openhft.chronicle.core.Jvm;

/**
 * Created by daniel on 08/03/2016.
 */
public class ExampleLatencyMain implements LatencyTask {
    int count = 0;
    double sin;
    //private NanoSampler nanoSamplerSin;
    //private NanoSampler nanoSamplerWait;
    private LatencyTestHarness lth;

    public static void main(String[] args) {
        LatencyTestHarness lth = new LatencyTestHarness()
                .warmUp(50_000)
                .messageCount(100_000)
                .throughput(25_000)
                .accountForCoordinatedOmmission(true)
                .runs(3)
                .accountForCoordinatedOmmission(true)
                .build(new ExampleLatencyMain());
        lth.start();
    }

    @Override
    public void run(long startTimeNS) {
        count++;
        if(count==160_000) {
            System.out.println("PAUSE");
            //long now = System.nanoTime();
            Jvm.pause(1000);
            //nanoSamplerWait.sampleNanos(System.nanoTime()-now);
        }

        long now = System.nanoTime();
        sin = Math.sin(count);
        //nanoSamplerSin.sampleNanos(System.nanoTime()-now);

        lth.sample(System.nanoTime()-startTimeNS);
    }

    @Override
    public void init(LatencyTestHarness lth) {

        this.lth = lth;
        //nanoSamplerSin = lth.addProbe("sin");
        //nanoSamplerWait = lth.addProbe("busyWait");
    }

    @Override
    public void complete() {
    }
}
