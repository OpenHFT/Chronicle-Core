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
