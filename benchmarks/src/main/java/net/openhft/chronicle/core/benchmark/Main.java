/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core.benchmark;

import net.openhft.chronicle.core.Jvm;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/*
 * Created by Peter Lawrey on 11/08/15.
 *
 * Windows 10

 Benchmark                                       Mode      Cnt      Score   Error  Units
Main.systemNanoTime                           sample  1071824     24.531 ± 0.566  ns/op
Main.systemNanoTime:systemNanoTime·p0.00      sample                 ≈ 0          ns/op
Main.systemNanoTime:systemNanoTime·p0.50      sample                 ≈ 0          ns/op
Main.systemNanoTime:systemNanoTime·p0.90      sample                 ≈ 0          ns/op
Main.systemNanoTime:systemNanoTime·p0.95      sample             284.000          ns/op
Main.systemNanoTime:systemNanoTime·p0.99      sample             285.000          ns/op
Main.systemNanoTime:systemNanoTime·p0.999     sample             285.000          ns/op
Main.systemNanoTime:systemNanoTime·p0.9999    sample            1422.000          ns/op
Main.systemNanoTime:systemNanoTime·p1.00      sample           73088.000          ns/op
Main.threadLocal_get                          sample  1267035     19.575 ± 0.365  ns/op
Main.threadLocal_get:threadLocal_get·p0.00    sample                 ≈ 0          ns/op
Main.threadLocal_get:threadLocal_get·p0.50    sample                 ≈ 0          ns/op
Main.threadLocal_get:threadLocal_get·p0.90    sample                 ≈ 0          ns/op
Main.threadLocal_get:threadLocal_get·p0.95    sample             284.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.99    sample             285.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.999   sample             285.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.9999  sample             285.000          ns/op
Main.threadLocal_get:threadLocal_get·p1.00    sample           16480.000          ns/op

 * Centos 7, Linux 3.10.0-514.26.2.el7.x86_64, due to Firmware bug relating to the X299 chipset.
 *
Benchmark                                       Mode      Cnt       Score    Error  Units
Main.systemNanoTime                           sample  1202315   10292.618 ± 13.440  ns/op
Main.systemNanoTime:systemNanoTime·p0.00      sample             8944.000           ns/op
Main.systemNanoTime:systemNanoTime·p0.50      sample             8992.000           ns/op
Main.systemNanoTime:systemNanoTime·p0.90      sample            13488.000           ns/op
Main.systemNanoTime:systemNanoTime·p0.95      sample            17984.000           ns/op
Main.systemNanoTime:systemNanoTime·p0.99      sample            35968.000           ns/op
Main.systemNanoTime:systemNanoTime·p0.999     sample            54955.776           ns/op
Main.systemNanoTime:systemNanoTime·p0.9999    sample            72832.000           ns/op
Main.systemNanoTime:systemNanoTime·p1.00      sample           180736.000           ns/op
Main.threadLocal_get                          sample  1502246    5126.690 ±  7.549  ns/op
Main.threadLocal_get:threadLocal_get·p0.00    sample             4456.000           ns/op
Main.threadLocal_get:threadLocal_get·p0.50    sample             4496.000           ns/op
Main.threadLocal_get:threadLocal_get·p0.90    sample             4536.000           ns/op
Main.threadLocal_get:threadLocal_get·p0.95    sample             8992.000           ns/op
Main.threadLocal_get:threadLocal_get·p0.99    sample            13488.000           ns/op
Main.threadLocal_get:threadLocal_get·p0.999   sample            40448.000           ns/op
Main.threadLocal_get:threadLocal_get·p0.9999  sample            55168.000           ns/op
Main.threadLocal_get:threadLocal_get·p1.00    sample           103552.000           ns/op

* Centos 7, Linux 4.12.8-1.el7.elrepo.x86_64

* Benchmark                                       Mode      Cnt      Score   Error  Units
Main.threadLocal_get                          sample  1747321     28.047 ± 0.400  ns/op
Main.threadLocal_get:threadLocal_get·p0.00    sample              17.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.50    sample              24.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.90    sample              34.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.95    sample              36.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.99    sample              38.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.999   sample              43.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.9999  sample           10544.000          ns/op
Main.threadLocal_get:threadLocal_get·p1.00    sample           17184.000          ns/op
 */
@State(Scope.Thread)
public class Main {

    ThreadLocal threadLocal = ThreadLocal.withInitial(Object::new);
    Object ob = getClass();
    Object oc = System.getenv();

    public static void main(String... args) throws RunnerException, InvocationTargetException, IllegalAccessException {
        if (Jvm.isDebug()) {
            Main main = new Main();
            for (Method m : Main.class.getMethods()) {
                if (m.getAnnotation(Benchmark.class) != null) {
                    m.invoke(main);
                }
            }
        } else {
            int time = Boolean.getBoolean("longTest") ? 30 : 1;
            System.out.println("measurementTime: " + time + " secs");
            Options opt = new OptionsBuilder()
                    .include(Main.class.getSimpleName())
                    .warmupIterations(5)
                    .measurementIterations(5)
                    .forks(5)
                    .mode(Mode.SampleTime)
                    .measurementTime(TimeValue.seconds(time))
                    .timeUnit(TimeUnit.NANOSECONDS)
                    .build();

            new Runner(opt).run();
        }
    }

    //    @Benchmark
    public Thread currentThread() {
        return Thread.currentThread();
    }

    //    @Benchmark
    public long currentThread_getId() {
        return Thread.currentThread().getId();
    }

    //@Benchmark
    public Object threadLocal_get() {
        return threadLocal.get();
    }

    //    @Benchmark
    public long systemNanoTime() {
        return System.nanoTime();
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.Throughput})
    public void nullCheck() {
        if (threadLocal == null) throw new NullPointerException();
        if (ob == null) throw new NullPointerException();
        if (oc == null) throw new NullPointerException();
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.Throughput})
    public void nullCheck2(Blackhole bc) {
        threadLocal.getClass();
        ob.getClass();
        oc.getClass();
    }
}

