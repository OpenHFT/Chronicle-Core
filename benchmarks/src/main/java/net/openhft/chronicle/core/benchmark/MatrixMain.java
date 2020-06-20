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
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.BigMatrix;
import org.apache.commons.math.linear.BigMatrixImpl;
import org.apache.commons.math.linear.RealMatrix;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class MatrixMain {

    BigDecimal[][] values = new BigDecimal[32][32];
    BigDecimal[][] vector = new BigDecimal[32][1];
    double[][] valuesD = new double[32][32];
    double[][] vectorD = new double[32][1];

    {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                values[i][j] = BigDecimal.valueOf(r.nextDouble() - r.nextDouble()).setScale(6, RoundingMode.FLOOR);
                valuesD[i][j] = values[i][j].doubleValue();
            }
            vector[i][0] = BigDecimal.valueOf(r.nextInt(10_000_000) - r.nextInt(10_000_000));
            vectorD[i][0] = vector[i][0].doubleValue();
        }
    }

    public static void main(String... args) throws RunnerException, InvocationTargetException, IllegalAccessException {
        if (Jvm.isDebug()) {
            MatrixMain main = new MatrixMain();
            for (Method m : MatrixMain.class.getMethods()) {
                if (m.getAnnotation(Benchmark.class) != null) {
                    m.invoke(main);
                }
            }
        } else {
            int time = Jvm.getBoolean("longTest") ? 60 : 10;
            System.out.println("measurementTime: " + time + " secs");
            Options opt = new OptionsBuilder()
                    .include(".*" + MatrixMain.class.getSimpleName() + ".*")
                    .warmupIterations(3)
                    .measurementIterations(6)
                    .forks(5)
                    .mode(Mode.SampleTime)
                    .measurementTime(TimeValue.seconds(time))
                    .timeUnit(TimeUnit.MICROSECONDS)
                    .build();

            new Runner(opt).run();
        }
    }

    @Benchmark
//    @BenchmarkMode({Mode.SampleTime, Mode.Throughput})
    public BigMatrix BigDecimalMatrix() {
        BigMatrix matrix = new BigMatrixImpl(values);
        BigMatrix matrix2 = new BigMatrixImpl(vector);
        return matrix.multiply(matrix2);
    }

    @Benchmark
//    @BenchmarkMode({Mode.SampleTime, Mode.Throughput})
    public RealMatrix DoubleMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(valuesD);
        RealMatrix matrix2 = new Array2DRowRealMatrix(vectorD);
        return matrix.multiply(matrix2);
    }
}

