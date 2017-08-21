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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
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
   Percentiles, ns/op:
      p(0.0000) =   4456.000 ns/op
     p(50.0000) =   4496.000 ns/op
     p(90.0000) =   4536.000 ns/op
     p(95.0000) =   8992.000 ns/op
     p(99.0000) =  16080.000 ns/op
     p(99.9000) =  40448.000 ns/op
     p(99.9900) =  71936.000 ns/op
     p(99.9990) = 126117.797 ns/op
     p(99.9999) = 190711.637 ns/op
    p(100.0000) = 247296.000 ns/op


# Run complete. Total time: 00:05:09

Benchmark                                       Mode      Cnt       Score   Error  Units
Main.threadLocal_get                          sample  3590156    5126.819 ± 5.218  ns/op
Main.threadLocal_get:threadLocal_get·p0.00    sample             4456.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.50    sample             4496.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.90    sample             4536.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.95    sample             8992.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.99    sample            16080.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.999   sample            40448.000          ns/op
Main.threadLocal_get:threadLocal_get·p0.9999  sample            71936.000          ns/op
Main.threadLocal_get:threadLocal_get·p1.00    sample           247296.000          ns/op

 */
@State(Scope.Thread)
public class Main {

    ThreadLocal threadLocal = ThreadLocal.withInitial(Object::new);

    public static void main(String... args) throws RunnerException, InvocationTargetException, IllegalAccessException {
        if (Jvm.isDebug()) {
            Main main = new Main();
            for (Method m : Main.class.getMethods()) {
                if (m.getAnnotation(Benchmark.class) != null) {
                    m.invoke(main);
                }
            }
        } else {
            int time = Boolean.getBoolean("longTest") ? 30 : 2;
            System.out.println("measurementTime: " + time + " secs");
            Options opt = new OptionsBuilder()
                    .include(Main.class.getSimpleName())
//                    .warmupIterations(5)
                    .measurementIterations(5)
                    .forks(10)
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

    @Benchmark
    public Object threadLocal_get() {
        return threadLocal.get();
    }
}

