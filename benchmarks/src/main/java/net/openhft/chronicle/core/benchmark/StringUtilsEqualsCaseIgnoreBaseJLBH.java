/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.benchmark;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.StringUtils;
import net.openhft.chronicle.jlbh.JLBH;
import net.openhft.chronicle.jlbh.JLBHOptions;
import net.openhft.chronicle.jlbh.JLBHTask;
import net.openhft.chronicle.jlbh.TeamCityHelper;

import java.util.function.Supplier;

public class StringUtilsEqualsCaseIgnoreBaseJLBH implements JLBHTask {

    private final Class<?> klass;
    private final CharSequence left;
    private final CharSequence right;
    private final int iterations;
    private JLBH jlbh;

    private StringUtilsEqualsCaseIgnoreBaseJLBH(Class<?> klass,
                                                CharSequence left,
                                                CharSequence right,
                                                int iterations) {
        this.klass = klass;
        this.left = left;
        this.right = right;
        this.iterations = iterations;
    }

    @Override
    public void init(JLBH jlbh) {
        this.jlbh = jlbh;
    }

    @Override
    public void run(long startTimeNS) {
        StringUtils.equalsCaseIgnore(left, right);
        jlbh.sample(System.nanoTime() - startTimeNS);
    }

    @Override
    public void complete() {
        TeamCityHelper.teamCityStatsLastRun(klass.getSimpleName(), jlbh, iterations, System.out);
    }

    public static void run(Class<?> klass,
                           Supplier<CharSequence> left,
                           Supplier<CharSequence> right) {
        System.setProperty("jvm.resource.tracing", "false");
        Jvm.init();
        final int throughput = Integer.getInteger("throughput", 500_000);
        final int iterations = Integer.getInteger("iterations", 10_000_000);
        final int warmup = Integer.getInteger("warmup", 5_000_000);
        final int runs = Integer.getInteger("runs", 4);
        JLBHOptions jlbhOptions = new JLBHOptions().
                runs(runs).
                warmUpIterations(warmup).
                throughput(throughput).
                iterations(iterations).
                pauseAfterWarmupMS(100).
                recordOSJitter(false).
                jlbhTask(new StringUtilsEqualsCaseIgnoreBaseJLBH(klass, left.get(), right.get(), iterations));
        JLBH jlbh = new JLBH(jlbhOptions);
        jlbh.start();
    }

    public static CharSequence generate(Supplier<Character> characterSupplier, int length) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buffer.append(characterSupplier.get());
        }
        return buffer.toString();
    }
}
