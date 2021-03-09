/*
 *     Copyright (C) 2015-2020 chronicle.software
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
import net.openhft.chronicle.core.UnsafeMemory;
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

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

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

    //    ThreadLocal threadLocal = ThreadLocal.withInitial(Object::new);
//    Object ob = getClass();
//    Object oc = System.getenv();
/*
    int[] size = {
            0, 1, 2, 3, 5, 8, 1, 2, 3,
            1, 2, 3, 4, 6, 4, 8, 0, 4};
        int[] size = {
                0, 1, 2, 3, 5, 8, 13, 21, 31,
                1, 2, 3, 4, 6, 9, 14, 22, 32};
*/
    int[] size = {
            11, 13, 21, 31, 9, 14, 22, 32};


    long addr0 = UNSAFE.allocateMemory(160);
    long[] addr = {addr0, addr0 + 40, addr0 + 80, addr0 + 120};
    byte[][] bytes = new byte[4][40];
    char[][] chars = new char[4][40];
    int count = 0;
    private int s, o, o2;

    public Main() {
        bytes[1][1] = (byte) 0x80;
        bytes[2][13] = (byte) 0x80;
        bytes[3][20] = (byte) 0x80;

        chars[1][1] = (char) 0x80;
        chars[2][13] = (char) 0x80;
        chars[3][20] = (char) 0x80;

        UNSAFE.putByte(addr[1] + 1, (byte) 0x80);
        UNSAFE.putByte(addr[2] + 13, (byte) 0x80);
        UNSAFE.putByte(addr[3] + 20, (byte) 0x80);
    }

    public static void main(String... args) throws RunnerException, InvocationTargetException, IllegalAccessException {
        if (Jvm.isDebug()) {
            Main main = new Main();
            for (Method m : Main.class.getMethods()) {
                if (m.getAnnotation(Benchmark.class) != null) {
                    m.invoke(main);
                }
            }
        } else {
            int time = Jvm.getBoolean("longTest") ? 30 : 1;
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

/*    @Setup
    public void setup() {
        o = count & 7;
        o2 = (count >> 3) & 7;
        count++;
        Jvm.nanoPause();
    }*/

    //    @Benchmark
    public byte[] partialBytes() {
        for (int s : size) {
            final long l = UnsafeMemory.INSTANCE.partialRead(bytes[0], o, s);
            UnsafeMemory.INSTANCE.partialWrite(bytes[1], o2, l, s);
        }
        return bytes[1];
    }

    //    @Benchmark
    public long partialAddr() {
        for (int s : size) {
            final long l = UnsafeMemory.INSTANCE.partialRead(addr[0] + o, s);
            UnsafeMemory.INSTANCE.partialWrite(addr[1] + o2, l, s);
        }
        return UnsafeMemory.UNSAFE.getLong(addr[1]);
    }
    /*
    Benchmark                                 Mode       Cnt      Score   Error  Units
Main.partialAddr                        sample  25307010     80.135 ± 0.153  ns/op
Main.partialAddr:partialAddr·p0.00      sample               55.000          ns/op
Main.partialAddr:partialAddr·p0.50      sample               76.000          ns/op
Main.partialAddr:partialAddr·p0.90      sample               85.000          ns/op
Main.partialAddr:partialAddr·p0.95      sample               88.000          ns/op
Main.partialAddr:partialAddr·p0.99      sample               95.000          ns/op
Main.partialAddr:partialAddr·p0.999     sample              161.000          ns/op
Main.partialAddr:partialAddr·p0.9999    sample            15968.000          ns/op
Main.partialAddr:partialAddr·p1.00      sample            82944.000          ns/op
Main.partialBytes                       sample  23993054     87.231 ± 0.160  ns/op
Main.partialBytes:partialBytes·p0.00    sample               56.000          ns/op
Main.partialBytes:partialBytes·p0.50    sample               85.000          ns/op
Main.partialBytes:partialBytes·p0.90    sample               95.000          ns/op
Main.partialBytes:partialBytes·p0.95    sample               97.000          ns/op
Main.partialBytes:partialBytes·p0.99    sample              100.000          ns/op
Main.partialBytes:partialBytes·p0.999   sample              130.000          ns/op
Main.partialBytes:partialBytes·p0.9999  sample            16016.000          ns/op
Main.partialBytes:partialBytes·p1.00    sample            70912.000          ns/op

Benchmark                                 Mode       Cnt       Score   Error  Units
Main.partialAddr                        sample  22409954      92.225 ± 0.177  ns/op
Main.partialAddr:partialAddr·p0.00      sample                64.000          ns/op
Main.partialAddr:partialAddr·p0.50      sample                88.000          ns/op
Main.partialAddr:partialAddr·p0.90      sample                98.000          ns/op
Main.partialAddr:partialAddr·p0.95      sample               101.000          ns/op
Main.partialAddr:partialAddr·p0.99      sample               105.000          ns/op
Main.partialAddr:partialAddr·p0.999     sample               186.000          ns/op
Main.partialAddr:partialAddr·p0.9999    sample             17312.144          ns/op
Main.partialAddr:partialAddr·p1.00      sample             95744.000          ns/op
Main.partialBytes                       sample  23364166      87.952 ± 0.171  ns/op
Main.partialBytes:partialBytes·p0.00    sample                58.000          ns/op
Main.partialBytes:partialBytes·p0.50    sample                83.000          ns/op
Main.partialBytes:partialBytes·p0.90    sample                96.000          ns/op
Main.partialBytes:partialBytes·p0.95    sample                98.000          ns/op
Main.partialBytes:partialBytes·p0.99    sample               109.000          ns/op
Main.partialBytes:partialBytes·p0.999   sample               162.000          ns/op
Main.partialBytes:partialBytes·p0.9999  sample             16377.333          ns/op
Main.partialBytes:partialBytes·p1.00    sample            159232.000          ns/op

Benchmark                                 Mode       Cnt       Score   Error  Units
Main.partialAddr                        sample  17319649     117.593 ± 0.257  ns/op
Main.partialAddr:partialAddr·p0.00      sample                82.000          ns/op
Main.partialAddr:partialAddr·p0.50      sample               113.000          ns/op
Main.partialAddr:partialAddr·p0.90      sample               119.000          ns/op
Main.partialAddr:partialAddr·p0.95      sample               125.000          ns/op
Main.partialAddr:partialAddr·p0.99      sample               131.000          ns/op
Main.partialAddr:partialAddr·p0.999     sample               235.000          ns/op
Main.partialAddr:partialAddr·p0.9999    sample             19968.000          ns/op
Main.partialAddr:partialAddr·p1.00      sample             77824.000          ns/op
Main.partialBytes                       sample  18351980     127.364 ± 0.278  ns/op
Main.partialBytes:partialBytes·p0.00    sample                82.000          ns/op
Main.partialBytes:partialBytes·p0.50    sample               118.000          ns/op
Main.partialBytes:partialBytes·p0.90    sample               141.000          ns/op
Main.partialBytes:partialBytes·p0.95    sample               145.000          ns/op
Main.partialBytes:partialBytes·p0.99    sample               165.000          ns/op
Main.partialBytes:partialBytes·p0.999   sample               494.000          ns/op
Main.partialBytes:partialBytes·p0.9999  sample             21024.000          ns/op
Main.partialBytes:partialBytes·p1.00    sample            289280.000          ns/op
     */

    @Benchmark
    public boolean is7bitBytes() {
        int i = count & 3;
        int s = size[(count >> 2) & 7];
        int o = (count >> 6) & 7;
        count++;
        return UnsafeMemory.INSTANCE.is7Bit(bytes[i], o, s);
    }

    //    @Benchmark
    public boolean is7bitChars() {
        int i = count & 3;
        int s = size[(count >> 2) & 15];
        int o = (count >> 6) & 7;
        count++;
        return UnsafeMemory.INSTANCE.is7Bit(chars[i], o, s);
    }

    @Benchmark
    public boolean is7bitAddr() {
        int i = count & 3;
        int s = size[(count >> 2) & 7];
        int o = (count >> 6) & 7;
        count++;
        return UnsafeMemory.INSTANCE.is7Bit(addr[i] + o, s);
    }
}
/*
Sizes 9-32
Benchmark                               Mode       Cnt      Score   Error  Units
Main.is7bitAddr                       sample  20241286     31.549 ± 0.161  ns/op
Main.is7bitAddr:is7bitAddr·p0.00      sample               18.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.50      sample               29.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.90      sample               36.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.95      sample               37.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.99      sample               40.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.999     sample              107.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.9999    sample            15664.000          ns/op
Main.is7bitAddr:is7bitAddr·p1.00      sample            64128.000          ns/op
Main.is7bitBytes                      sample  20139163     32.905 ± 0.161  ns/op
Main.is7bitBytes:is7bitBytes·p0.00    sample               18.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.50    sample               31.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.90    sample               38.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.95    sample               39.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.99    sample               42.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.999   sample               91.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.9999  sample            15760.000          ns/op
Main.is7bitBytes:is7bitBytes·p1.00    sample            56512.000          ns/op
 */

/*
run x16 8*4?1*
Benchmark                               Mode       Cnt      Score   Error  Units
Main.is7bitAddr                       sample  21618532     29.449 ± 0.117  ns/op
Main.is7bitAddr:is7bitAddr·p0.00      sample               16.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.50      sample               27.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.90      sample               37.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.95      sample               38.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.99      sample               40.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.999     sample               48.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.9999    sample             1174.000          ns/op
Main.is7bitAddr:is7bitAddr·p1.00      sample            93568.000          ns/op
Main.is7bitBytes                      sample  20642426     29.680 ± 0.119  ns/op
Main.is7bitBytes:is7bitBytes·p0.00    sample               16.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.50    sample               29.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.90    sample               36.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.95    sample               37.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.99    sample               39.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.999   sample               46.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.9999  sample             1230.000          ns/op
Main.is7bitBytes:is7bitBytes·p1.00    sample            65088.000          ns/op
Main.is7bitChars                      sample  19697567     30.924 ± 0.113  ns/op
Main.is7bitChars:is7bitChars·p0.00    sample               18.000          ns/op
Main.is7bitChars:is7bitChars·p0.50    sample               30.000          ns/op
Main.is7bitChars:is7bitChars·p0.90    sample               38.000          ns/op
Main.is7bitChars:is7bitChars·p0.95    sample               39.000          ns/op
Main.is7bitChars:is7bitChars·p0.99    sample               41.000          ns/op
Main.is7bitChars:is7bitChars·p0.999   sample               48.000          ns/op
Main.is7bitChars:is7bitChars·p0.9999  sample             1148.000          ns/op
Main.is7bitChars:is7bitChars·p1.00    sample            34176.000          ns/op

8*1*
Benchmark                               Mode       Cnt      Score   Error  Units
Main.is7bitAddr                       sample  20659644     32.466 ± 0.117  ns/op
Main.is7bitAddr:is7bitAddr·p0.00      sample               16.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.50      sample               33.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.90      sample               38.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.95      sample               39.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.99      sample               42.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.999     sample               81.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.9999    sample             1294.000          ns/op
Main.is7bitAddr:is7bitAddr·p1.00      sample            56000.000          ns/op
Main.is7bitBytes                      sample  20455017     30.001 ± 0.110  ns/op
Main.is7bitBytes:is7bitBytes·p0.00    sample               16.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.50    sample               28.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.90    sample               38.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.95    sample               41.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.99    sample               46.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.999   sample               52.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.9999  sample             1130.000          ns/op
Main.is7bitBytes:is7bitBytes·p1.00    sample            42240.000          ns/op
Main.is7bitChars                      sample  20287788     30.711 ± 0.116  ns/op
Main.is7bitChars:is7bitChars·p0.00    sample               16.000          ns/op
Main.is7bitChars:is7bitChars·p0.50    sample               30.000          ns/op
Main.is7bitChars:is7bitChars·p0.90    sample               37.000          ns/op
Main.is7bitChars:is7bitChars·p0.95    sample               38.000          ns/op
Main.is7bitChars:is7bitChars·p0.99    sample               40.000          ns/op
Main.is7bitChars:is7bitChars·p0.999   sample               47.000          ns/op
Main.is7bitChars:is7bitChars·p0.9999  sample             1266.000          ns/op
Main.is7bitChars:is7bitChars·p1.00    sample            34944.000          ns/op

4*1*
Benchmark                               Mode       Cnt      Score   Error  Units
Main.is7bitAddr                       sample  21388584     31.677 ± 0.115  ns/op
Main.is7bitAddr:is7bitAddr·p0.00      sample               16.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.50      sample               31.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.90      sample               37.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.95      sample               37.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.99      sample               39.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.999     sample               51.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.9999    sample             1222.283          ns/op
Main.is7bitAddr:is7bitAddr·p1.00      sample            43712.000          ns/op
Main.is7bitBytes                      sample  20922098     32.664 ± 0.118  ns/op
Main.is7bitBytes:is7bitBytes·p0.00    sample               16.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.50    sample               33.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.90    sample               37.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.95    sample               38.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.99    sample               40.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.999   sample               46.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.9999  sample             1318.000          ns/op
Main.is7bitBytes:is7bitBytes·p1.00    sample            73088.000          ns/op
Main.is7bitChars                      sample  19041789     29.981 ± 0.128  ns/op
Main.is7bitChars:is7bitChars·p0.00    sample               16.000          ns/op
Main.is7bitChars:is7bitChars·p0.50    sample               28.000          ns/op
Main.is7bitChars:is7bitChars·p0.90    sample               35.000          ns/op
Main.is7bitChars:is7bitChars·p0.95    sample               37.000          ns/op
Main.is7bitChars:is7bitChars·p0.99    sample               40.000          ns/op
Main.is7bitChars:is7bitChars·p0.999   sample               46.000          ns/op
Main.is7bitChars:is7bitChars·p0.9999  sample             1490.000          ns/op
Main.is7bitChars:is7bitChars·p1.00    sample            52096.000          ns/op

1*
Benchmark                               Mode       Cnt       Score   Error  Units
Main.is7bitAddr                       sample  19070168      32.969 ± 0.129  ns/op
Main.is7bitAddr:is7bitAddr·p0.00      sample                16.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.50      sample                33.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.90      sample                39.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.95      sample                41.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.99      sample                45.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.999     sample                55.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.9999    sample              1549.966          ns/op
Main.is7bitAddr:is7bitAddr·p1.00      sample             34816.000          ns/op
Main.is7bitBytes                      sample  17351169      34.477 ± 0.145  ns/op
Main.is7bitBytes:is7bitBytes·p0.00    sample                16.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.50    sample                34.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.90    sample                42.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.95    sample                43.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.99    sample                47.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.999   sample                80.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.9999  sample              1918.000          ns/op
Main.is7bitBytes:is7bitBytes·p1.00    sample            110592.000          ns/op
Main.is7bitChars                      sample  16034178      34.506 ± 0.158  ns/op
Main.is7bitChars:is7bitChars·p0.00    sample                16.000          ns/op
Main.is7bitChars:is7bitChars·p0.50    sample                33.000          ns/op
Main.is7bitChars:is7bitChars·p0.90    sample                40.000          ns/op
Main.is7bitChars:is7bitChars·p0.95    sample                43.000          ns/op
Main.is7bitChars:is7bitChars·p0.99    sample                49.000          ns/op
Main.is7bitChars:is7bitChars·p0.999   sample                51.000          ns/op
Main.is7bitChars:is7bitChars·p0.9999  sample             15312.000          ns/op
Main.is7bitChars:is7bitChars·p1.00    sample             82176.000          ns/op

8*
Benchmark                                         Mode       Cnt       Score   Error  Units
Main.is7bitBytesWhole                           sample  25138202      32.632 ± 0.116  ns/op
Main.is7bitBytesWhole:is7bitBytesWhole·p0.00    sample                15.000          ns/op
Main.is7bitBytesWhole:is7bitBytesWhole·p0.50    sample                34.000          ns/op
Main.is7bitBytesWhole:is7bitBytesWhole·p0.90    sample                36.000          ns/op
Main.is7bitBytesWhole:is7bitBytesWhole·p0.95    sample                37.000          ns/op
Main.is7bitBytesWhole:is7bitBytesWhole·p0.99    sample                38.000          ns/op
Main.is7bitBytesWhole:is7bitBytesWhole·p0.999   sample                54.000          ns/op
Main.is7bitBytesWhole:is7bitBytesWhole·p0.9999  sample              1280.359          ns/op
Main.is7bitBytesWhole:is7bitBytesWhole·p1.00    sample            195840.000          ns/op

8*4?2?1?
Benchmark                               Mode       Cnt      Score   Error  Units
Main.is7bitAddr                       sample  21480344     32.600 ± 0.130  ns/op
Main.is7bitAddr:is7bitAddr·p0.00      sample               17.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.50      sample               32.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.90      sample               38.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.95      sample               40.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.99      sample               44.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.999     sample               82.000          ns/op
Main.is7bitAddr:is7bitAddr·p0.9999    sample             1868.000          ns/op
Main.is7bitAddr:is7bitAddr·p1.00      sample            62528.000          ns/op
Main.is7bitBytes                      sample  20565335     31.581 ± 0.129  ns/op
Main.is7bitBytes:is7bitBytes·p0.00    sample               16.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.50    sample               31.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.90    sample               37.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.95    sample               38.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.99    sample               40.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.999   sample               48.000          ns/op
Main.is7bitBytes:is7bitBytes·p0.9999  sample             1921.597          ns/op
Main.is7bitBytes:is7bitBytes·p1.00    sample            58624.000          ns/op
Main.is7bitChars                      sample  20109696     31.567 ± 0.131  ns/op
Main.is7bitChars:is7bitChars·p0.00    sample               16.000          ns/op
Main.is7bitChars:is7bitChars·p0.50    sample               29.000          ns/op
Main.is7bitChars:is7bitChars·p0.90    sample               38.000          ns/op
Main.is7bitChars:is7bitChars·p0.95    sample               40.000          ns/op
Main.is7bitChars:is7bitChars·p0.99    sample               43.000          ns/op
Main.is7bitChars:is7bitChars·p0.999   sample               50.000          ns/op
Main.is7bitChars:is7bitChars·p0.9999  sample             1900.182          ns/op
Main.is7bitChars:is7bitChars·p1.00    sample            65408.000          ns/op
 */
