/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.time;

/**
 * EXPERIMENTAL!!!
 */
public enum BackgroundTimeProvider implements TimeProvider {
    INSTANCE;

    static volatile long delta;
    static int count = 0;

    static {
        delta = SystemTimeProvider.INSTANCE.delta;
        Thread t = new Thread(INSTANCE::run, "bg-time-provider");
        t.setDaemon(true);
        t.start();
    }

    void run() {
        try {
            for (int i = 0; i < 1000; i++) {
                delta = SystemTimeProvider.INSTANCE.delta;
                Thread.yield();
            }
            for (; ; ) {
                long delta2 = SystemTimeProvider.INSTANCE.delta;
                delta += (delta2 - delta) / 8;
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            // dying.
        }
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1000;
    }

    @Override
    public long currentTimeNanos() {
        if (count++ > 20) {
            count = 0;
            return SystemTimeProvider.INSTANCE.currentTimeNanos();
        }
        return System.nanoTime() + delta;
    }
}
