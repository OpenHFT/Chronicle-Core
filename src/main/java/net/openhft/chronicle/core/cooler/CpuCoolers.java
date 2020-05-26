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
package net.openhft.chronicle.core.cooler;

import net.openhft.affinity.Affinity;
import net.openhft.chronicle.core.Jvm;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.locks.LockSupport;

public enum CpuCoolers implements CpuCooler {
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
    AFFINITY {
        boolean toogle;

        @Override
        public void disturb() {
            Affinity.setAffinity(toogle ? 3 : 4);
            toogle = !toogle;
        }
    },
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
    MEMORY_COPY {
        long[] from = new long[8 << 20];
        long[] to = new long[8 << 20];

        @Override
        public void disturb() {
            System.arraycopy(from, 0, to, 0, from.length);
        }
    },
    ALL {
        @Override
        public void disturb() {
            SERIALIZATION.disturb();
            MEMORY_COPY.disturb();
            PAUSE10.disturb();
        }
    };
    static volatile Object blackhole;

    public static void busyWait(double nanos) {
        long start = System.nanoTime();
        if (Jvm.isJava9Plus()) {
            while (System.nanoTime() - start < nanos) {
                Thread.holdsLock("");
            }
        } else {
            while (System.nanoTime() - start < nanos) {
                Compiler.enable();
            }
        }
    }
}
