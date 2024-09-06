/*
 * Copyright 2016-2020 chronicle.software
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
 * different way. The exact way in which the CPU is disturbed is defined by the {@link #disturb()}
 * method of each enum constant.
 * <p>
 * These coolers simulate different types of CPU work, such as parking the thread, busy-waiting,
 * or serializing and deserializing objects. Some constants involve waiting, others involve spinning
 * on work, while some engage in memory operations.
 */
public enum CpuCoolers implements CpuCooler {
    /**
     * Causes the CPU to wait without doing work for a very short period of time.
     * Uses {@link LockSupport#parkNanos(long)} to pause the thread for 200,000 nanoseconds.
     */
    PARK {
        @Override
        public void disturb() {
            LockSupport.parkNanos(200_000);
        }
    },

    /**
     * Causes the CPU to pause for 1 nanosecond.
     * Uses {@link Jvm#pause(long)} to simulate work.
     */
    PAUSE1 {
        @Override
        public void disturb() {
            Jvm.pause(1);
        }
    },

    // Additional CPU coolers for varying pause durations

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

    /**
     * Causes the CPU to yield control to other threads, simulating a disturbance by thread yielding.
     */
    YIELD {
        @Override
        public void disturb() {
            Thread.yield();
        }
    },

    // Various CPU coolers that simulate busy-waiting, performing work in the loop

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
     * Switches the CPU affinity between two cores (core 3 and core 4).
     * This simulates a disturbance by causing the thread to switch between cores.
     */
    AFFINITY {
        boolean toogle;

        @Override
        public void disturb() {
            Affinity.setAffinity(toogle ? 3 : 4);
            toogle = !toogle;
        }
    },

    /**
     * Serializes and deserializes system properties using XML.
     * This simulates CPU disturbance through memory and I/O operations.
     */
    SERIALIZATION {
        @Override
        public void disturb() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLEncoder oos = new XMLEncoder(out);
            oos.writeObject(System.getProperties());
            oos.close();
            XMLDecoder ois = new XMLDecoder(new ByteArrayInputStream(out.toByteArray()));
            blackhole = ois.readObject();  // Store result in blackhole to avoid optimization
        }
    },

    /**
     * Copies a large array of longs from one array to another.
     * This simulates a memory operation disturbance by repeatedly copying large blocks of memory.
     */
    MEMORY_COPY {
        final long[] from = new long[8 << 20];  // Source array of 8MB
        final long[] to = new long[8 << 20];    // Destination array of 8MB

        @Override
        public void disturb() {
            System.arraycopy(from, 0, to, 0, from.length);  // Copy entire array
        }
    },

    /**
     * Performs multiple disturbances: serialization, memory copy, and pausing.
     * This combines different types of disturbances for a more complex CPU interaction.
     */
    ALL {
        @Override
        public void disturb() {
            SERIALIZATION.disturb();
            MEMORY_COPY.disturb();
            PAUSE10.disturb();
        }
    };

    /**
     * A shared volatile object to prevent certain disturbances from being optimized away by the JVM.
     */
    static volatile Object blackhole;

    /**
     * Performs a busy-wait for a specified number of nanoseconds.
     * This method repeatedly calls {@link Jvm#safepoint()} to avoid JVM optimizations.
     *
     * @param nanos The number of nanoseconds to busy-wait.
     */
    public static void busyWait(double nanos) {
        long start = System.nanoTime();
        while (System.nanoTime() - start < nanos) {
            Jvm.safepoint();
        }
    }
}
