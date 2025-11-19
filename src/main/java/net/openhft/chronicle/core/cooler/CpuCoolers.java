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
    PAUSE1 {
        @Override
        public void disturb() {
            Jvm.pause(1);
        }
    },
    PAUSE3 {
        @Override
        public void disturb() {
            Jvm.pause(3);
        }
    },
    PAUSE6 {
        @Override
        public void disturb() {
            Jvm.pause(6);
        }
    },
    PAUSE10 {
        @Override
        public void disturb() {
            Jvm.pause(10);
        }
    },
    PAUSE100 {
        @Override
        public void disturb() {
            Jvm.pause(100);
        }
    },
    PAUSE1000 {
        @Override
        public void disturb() {
            Jvm.pause(1000);
        }
    },
    YIELD {
        @Override
        public void disturb() {
            Thread.yield();
        }
    },
    BUSY {
        @Override
        public void disturb() {
            busyWait(0.1e6);
        }
    },
    BUSY_3 {
        @Override
        public void disturb() {
            busyWait(0.3e6);
        }
    },
    BUSY1 {
        @Override
        public void disturb() {
            busyWait(1e6);
        }
    },
    BUSY3 {
        @Override
        public void disturb() {
            busyWait(3e6);
        }
    },
    BUSY10 {
        @Override
        public void disturb() {
            busyWait(10e6);
        }
    },
    BUSY30 {
        @Override
        public void disturb() {
            busyWait(30e6);
        }
    },
    BUSY100 {
        @Override
        public void disturb() {
            busyWait(100e6);
        }
    },
    BUSY300 {
        @Override
        public void disturb() {
            busyWait(300e6);
        }
    },
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
    SERIALIZATION {
        private volatile Object lastRead;

        @Override
        public void disturb() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLEncoder oos = new XMLEncoder(out);
            oos.writeObject(System.getProperties());
            oos.close();
            XMLDecoder ois = new XMLDecoder(new ByteArrayInputStream(out.toByteArray()));
            lastRead = ois.readObject();
        }
    },
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

    public static void busyWait(double nanos) {
        long start = System.nanoTime();
        while (System.nanoTime() - start < nanos) {
            Jvm.safepoint();
        }
    }
}
