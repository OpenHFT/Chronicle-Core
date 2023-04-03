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

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StackTraceTest {
    private static final CountDownLatch threadStarted = new CountDownLatch(1);

    static void thinking() {
        threadStarted.countDown();
        Jvm.pause(5_000);
    }

    @Test
    public void forThread() throws InterruptedException {
        // load Jvm class before
        Jvm.init();
        Thread t = new Thread(StackTraceTest::thinking, "background");
        t.start();
        boolean started = threadStarted.await(1, TimeUnit.SECONDS);
        assertTrue(started);
        // wait for Jvm.pause to start
        Jvm.pause(50);
        StackTrace st = StackTrace.forThread(t);
        t.interrupt();
        if (Jvm.isJava20Plus()) {
            assertTrue(st.getMessage().endsWith("background,5,main] on main"));
            assertEquals("net.openhft.chronicle.core.Jvm.pause", st.getStackTrace()[1].toString().split("\\(")[0].replaceAll("^app//", ""));
        } else {
            assertEquals("Thread[background,5,main] on main", st.getMessage());
            assertEquals("net.openhft.chronicle.core.Jvm.pause", st.getStackTrace()[0].toString().split("\\(")[0].replaceAll("^app//", ""));
        }
    }
}
