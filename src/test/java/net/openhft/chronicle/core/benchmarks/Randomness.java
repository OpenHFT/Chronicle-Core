/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.benchmarks;

import net.openhft.chronicle.core.Maths;

import java.security.SecureRandom;
import java.util.Random;

public class Randomness {

    private static final Random RNG = new SecureRandom();

    // Long running, avg score = 446980825
    public static void main(String[] args) {
        long time = 0, timeCount = 0;
        long scoreSum = 0;
        for (int t = 0; t < 500; t++) {
            long[] hashs = new long[8192];
            StringBuilder sb = new StringBuilder();
            byte[] init = new byte[hashs.length / 64];
            RNG.nextBytes(init);
            for (int i = 0; i < hashs.length; i++) {
                sb.setLength(0);
                sb.append(t).append('-').append(i);
                long start = System.nanoTime();
                hashs[i] = Maths.hash64(sb);
                time += System.nanoTime() - start;
                timeCount++;
            }
            long score = 0;
            for (int i = 0; i < hashs.length - 1; i++)
                for (int j = i + 1; j < hashs.length; j++) {
                    long diff = hashs[j] ^ hashs[i];
                    int diffBC = Long.bitCount(diff);
                    if (diffBC < 18) {
                        long d = 1L << (17 - diffBC);
                        score += d;
                    }
                }
            scoreSum += score;
        }
        System.out.println("Average score: " + scoreSum / 500);
        double avgTimeMicros = (double) time / (double) timeCount / 1e3;
        System.out.printf("Average time %.3f us%n", avgTimeMicros);
    }
}
