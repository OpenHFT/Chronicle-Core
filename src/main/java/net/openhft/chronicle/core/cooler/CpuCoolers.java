/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cooler;

import net.openhft.affinity.Affinity;
import net.openhft.chronicle.core.Jvm;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.locks.LockSupport;

/**
 * An enumeration of various CPU cooler implementations, each of which disturbs the CPU in a
 * different way. The exact way in which the CPU is disturbed is defined by the `disturb()` method
 * of each enum constant.
 */
public enum CpuCoolers implements CpuCooler {
    /**
     * Causes the CPU to wait without doing work for a very short period of time.
     */
    PARK {
        @Override
        public void disturb() {
            LockSupport.parkNanos(200_000);
        }
    },
    /**
     * Causes the CPU to pause for roughly 1 nanosecond.
     */
    PAUSE1 {
        @Override
        public void disturb() {
            Jvm.pause(1);
        }
    },
    /**
     * Causes the CPU to pause for roughly 3 nanoseconds.
     */
    PAUSE3 {
        @Override
        public void disturb() {
            Jvm.pause(3);
        }
    },
    /**
     * Causes the CPU to pause for roughly 6 nanoseconds.
     */
    PAUSE6 {
        @Override
        public void disturb() {
            Jvm.pause(6);
        }
    },
    /**
     * Causes the CPU to pause for roughly 10 nanoseconds.
     */
    PAUSE10 {
        @Override
        public void disturb() {
            Jvm.pause(10);
        }
    },
    /**
     * Causes the CPU to pause for roughly 100 nanoseconds.
     */
    PAUSE100 {
        @Override
        public void disturb() {
            Jvm.pause(100);
        }
    },
    /**
     * Causes the CPU to pause for roughly 1 microsecond.
     */
    PAUSE1000 {
        @Override
        public void disturb() {
            Jvm.pause(1000);
        }
    },
    /**
     * Yields the thread to let other runnable threads proceed.
     */
    YIELD {
        @Override
        public void disturb() {
            Thread.yield();
        }
    },
    /**
     * Performs a short busy-spin to generate load.
     */
    BUSY {
        @Override
        public void disturb() {
            busyWait(0.1e6);
        }
    },
    /**
     * Busy-spin for approximately 0.3 ms.
     */
    BUSY_3 {
        @Override
        public void disturb() {
            busyWait(0.3e6);
        }
    },
    /**
     * Busy-spin for approximately 1 ms.
     */
    BUSY1 {
        @Override
        public void disturb() {
            busyWait(1e6);
        }
    },
    /**
     * Busy-spin for approximately 3 ms.
     */
    BUSY3 {
        @Override
        public void disturb() {
            busyWait(3e6);
        }
    },
    /**
     * Busy-spin for approximately 10 ms.
     */
    BUSY10 {
        @Override
        public void disturb() {
            busyWait(10e6);
        }
    },
    /**
     * Busy-spin for approximately 30 ms.
     */
    BUSY30 {
        @Override
        public void disturb() {
            busyWait(30e6);
        }
    },
    /**
     * Busy-spin for approximately 100 ms.
     */
    BUSY100 {
        @Override
        public void disturb() {
            busyWait(100e6);
        }
    },
    /**
     * Busy-spin for approximately 300 ms.
     */
    BUSY300 {
        @Override
        public void disturb() {
            busyWait(300e6);
        }
    },
    /**
     * Busy-spin for approximately 1 second.
     */
    BUSY1000 {
        @Override
        public void disturb() {
            busyWait(1000e6);
        }
    },
    /**
     * Switches the CPU affinity back and forth between two cores, causing the CPU to do work in
     * moving the executing thread from one core to the other.
     */
    AFFINITY {
        boolean toogle;

        @Override
        public void disturb() {
            Affinity.setAffinity(toogle ? 0 : 1);
            toogle = !toogle;
        }
    },
    /**
     * Performs repeated Java object serialisation/deserialisation to generate CPU work.
     */
    SERIALIZATION {
        @Override
        public void disturb() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLEncoder oos = new XMLEncoder(out);
            oos.writeObject(System.getProperties());
            oos.close();
            XMLDecoder ois = new XMLDecoder(new ByteArrayInputStream(out.toByteArray()));
            blackhole = ois.readObject();
        }
    },
    /**
     * Copies a large array to exercise memory bandwidth.
     */
    MEMORY_COPY {
        final long[] from = new long[8 << 20];
        final long[] to = new long[8 << 20];

        @Override
        public void disturb() {
            System.arraycopy(from, 0, to, 0, from.length);
        }
    },
    /**
     * Performs multiple disturbing operations at once.
     */
    ALL {
        @Override
        public void disturb() {
            SERIALIZATION.disturb();
            MEMORY_COPY.disturb();
            PAUSE10.disturb();
        }
    };
    static volatile Object blackhole;

    /**
     * Spins, periodically issuing safepoints, for roughly the requested duration.
     *
     * @param nanos approximate spin duration in nanoseconds
     */
    public static void busyWait(double nanos) {
        long start = System.nanoTime();
        while (System.nanoTime() - start < nanos) {
            Jvm.safepoint();
        }
    }
}
