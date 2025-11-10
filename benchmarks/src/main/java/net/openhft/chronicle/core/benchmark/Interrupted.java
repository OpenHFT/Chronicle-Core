//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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

@State(Scope.Thread)
public class Interrupted {
    Thread currentThread = null;
    MyThread myThread = new MyThread();

    public static void main(String... args) throws RunnerException, InvocationTargetException, IllegalAccessException {
        if (Jvm.isDebug()) {
            Interrupted main = new Interrupted();
            for (Method m : Interrupted.class.getMethods()) {
                if (m.getAnnotation(Benchmark.class) != null) {
                    m.invoke(main);
                }
            }
        } else {
            int time = Jvm.getBoolean("longTest") ? 30 : 1;
            System.out.println("measurementTime: " + time + " secs");
            Options opt = new OptionsBuilder()
                    .include(Interrupted.class.getSimpleName())
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

    @Benchmark
    public boolean interrupted() {
        if (currentThread == null)
            currentThread = Thread.currentThread();
        return currentThread.isInterrupted();
    }

    @Benchmark
    public boolean volatileBoolean() {
        // only included to be like to for like
        if (currentThread == null)
            currentThread = Thread.currentThread();
        // end of dummy code.
        return myThread.isInterrupted();
    }

    static class MyThread extends Thread {
        volatile boolean interrupted;

        @Override
        public void interrupt() {
            interrupted = true;
        }

        @Override
        public boolean isInterrupted() {
            return interrupted;
        }
    }
}
