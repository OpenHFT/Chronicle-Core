/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
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

package net.openhft.chronicle.core;

import java.util.concurrent.locks.LockSupport;

public class SleepTesterMain {
    public static void main(String[] args) throws InterruptedException {
        {
            long start = System.nanoTime();
            long count = 0;
            while (System.nanoTime() < start + 10e9) {
                for (int i = 0; i < 10; i++) {
                    LockSupport.parkNanos(1);
                    count++;
                }
            }
            long end = System.nanoTime();
            System.out.printf("LockSupport.parkNanos(1) took an average of %,d ns%n", (end - start) / count);
        }
        {
            long start = System.nanoTime();
            long count = 0;
            while (System.nanoTime() < start + 10e9) {
                for (int i = 0; i < 10; i++) {
                    Thread.yield();
                    count++;
                }
            }
            long end = System.nanoTime();
            System.out.printf("Thread.yield() took an average of %,d ns%n", (end - start) / count);
        }
        {
            long start = System.nanoTime();
            long count = 0;
            while (System.nanoTime() < start + 10e9) {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1);
                    count++;
                }
            }
            long end = System.nanoTime();
            System.out.printf("Thread.sleep(1) took an average of %,d ns%n", (end - start) / count);
        }
    }
}
