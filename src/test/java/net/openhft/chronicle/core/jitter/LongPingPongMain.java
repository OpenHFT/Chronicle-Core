/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.jitter;

import net.openhft.chronicle.core.util.Histogram;

public class LongPingPongMain {
    static volatile long pingTime = 0;
    static volatile long pingCount = 0;
    static volatile long pongCount = 0;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        Histogram h = new Histogram(32, 7);
        Thread pong = new Thread(() -> {
            while (running) {

            }
        });
    }
}
