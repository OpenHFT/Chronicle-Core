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

/* Java 8u251
Benchmark                                               Mode       Cnt      Score   Error  Units
MatrixMain.BigDecimalMatrix                           sample  67012042     13.564 ± 0.007  us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.00    sample               11.568          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.50    sample               13.216          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.90    sample               13.616          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.95    sample               13.824          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.99    sample               15.360          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.999   sample               30.848          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.9999  sample              966.656          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p1.00    sample            10485.760          us/op
MatrixMain.DoubleMatrix                               sample  69491622      3.348 ± 0.005  us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.00            sample                2.916          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.50            sample                3.156          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.90            sample                3.384          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.95            sample                3.460          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.99            sample                3.684          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.999           sample                7.312          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.9999          sample              876.544          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p1.00            sample            10797.056          us/op

Java 11.0.7
Benchmark                                               Mode        Cnt      Score   Error  Units
MatrixMain.BigDecimalMatrix                           sample   58719223     15.369 ± 0.004  us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.00    sample                13.712          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.50    sample                15.264          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.90    sample                15.856          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.95    sample                15.984          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.99    sample                17.632          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.999   sample                25.888          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.9999  sample               724.992          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p1.00    sample             10272.768          us/op
MatrixMain.DoubleMatrix                               sample  103905828      4.347 ± 0.002  us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.00            sample                 3.972          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.50            sample                 4.272          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.90            sample                 4.496          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.95            sample                 4.584          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.99            sample                 4.904          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.999           sample                 7.448          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.9999          sample                10.944          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p1.00            sample              8814.592          us/op

Java 14.0.1
Benchmark                                               Mode        Cnt     Score   Error  Units
MatrixMain.BigDecimalMatrix                           sample   58372272    15.484 ± 0.005  us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.00    sample               13.840          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.50    sample               15.264          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.90    sample               15.888          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.95    sample               16.080          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.99    sample               17.664          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.999   sample               24.608          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p0.9999  sample              988.160          us/op
MatrixMain.BigDecimalMatrix:BigDecimalMatrix·p1.00    sample             1773.568          us/op
MatrixMain.DoubleMatrix                               sample  102086153     4.433 ± 0.002  us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.00            sample                4.120          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.50            sample                4.352          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.90            sample                4.544          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.95            sample                4.616          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.99            sample                4.944          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.999           sample                7.464          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p0.9999          sample               13.312          us/op
MatrixMain.DoubleMatrix:DoubleMatrix·p1.00            sample             2037.760          us/op
*/
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
                    .measurementIterations(12)
                    .forks(30)
                    .mode(Mode.SampleTime)
                    .measurementTime(TimeValue.seconds(time))
                    .timeUnit(TimeUnit.MICROSECONDS)
                    .build();

            new Runner(opt).run();
        }
    }

//    @Benchmark
//    @BenchmarkMode({Mode.SampleTime, Mode.Throughput})
    public BigMatrix BigDecimalMatrix() {
        BigMatrix matrix = new BigMatrixImpl(values);
        BigMatrix matrix2 = new BigMatrixImpl(vector);
        return matrix.multiply(matrix2);
    }

    //    @Benchmark
//    @BenchmarkMode({Mode.SampleTime, Mode.Throughput})
    public RealMatrix DoubleMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(valuesD);
        RealMatrix matrix2 = new Array2DRowRealMatrix(vectorD);
        return matrix.multiply(matrix2);
    }
}
