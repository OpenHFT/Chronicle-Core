/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import java.util.concurrent.TimeUnit;

/**
 * Manual research probe for issue #331. This is deliberately not a unit test:
 * finalisation and GC timing are nondeterministic, and the disabled-finalisation
 * invocation requires a JVM launch flag.
 */
public final class FinalisationDisabledProbe {

    private static volatile boolean finalised;

    private FinalisationDisabledProbe() {
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 1 && "barrier".equals(args[0])) {
            probeFinalisationBarrier();
            return;
        }
        if (args.length != 0)
            throw new IllegalArgumentException("usage: FinalisationDisabledProbe [barrier]");
        probeFinaliserInvocation();
    }

    @SuppressWarnings({"deprecation", "removal"})
    private static void probeFinaliserInvocation() throws InterruptedException {
        Object candidate = new Object() {
            @SuppressWarnings({"deprecation", "removal", "java:S1113"})
            @Override
            protected void finalize() {
                finalised = true;
            }
        };
        candidate = null;

        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (!finalised && System.nanoTime() < deadline) {
            System.gc();
            System.runFinalization();
            Thread.sleep(10);
        }
        System.out.println("finalize ran = " + finalised);
    }

    private static void probeFinalisationBarrier() {
        final long start = System.nanoTime();
        try {
            AbstractCloseable.gcAndWaitForCloseablesToClose();
            System.out.println("barrier returned after " + elapsedMillis(start) + " ms");
        } catch (AssertionError error) {
            System.out.println(error + " after " + elapsedMillis(start) + " ms");
        }
    }

    private static long elapsedMillis(long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }
}
