/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimeTest {

    @Test
    public void testTickTime() throws InterruptedException {
        long startReal = System.currentTimeMillis();
        long start = Time.tickTime();
        for (int i = 0; i < 10; i++) {
            Jvm.pause(10);
            Time.tickTime();
        }
        long last = Time.tickTime();
        // 100ms have passed in real world but tickTime says 11ms
        long ticksElapsed = last - start;
        assertEquals(11, ticksElapsed, 1);
        long realTimeElapsed = System.currentTimeMillis() - startReal;
        System.out.println("tickTimeElapsed " + ticksElapsed + " realTimeElapsed " + realTimeElapsed);
        assertTrue("tickTime should progress slower than real time", ticksElapsed < realTimeElapsed);
    }

    @Test
    public void testFastTime() throws InterruptedException {
        long startReal = System.currentTimeMillis();
        long start = Time.tickTime();
        for (int i = 0; i < 100; i++) {
            Jvm.pause(1);
            Time.tickTime();
        }
        long last = Time.tickTime();
        long ticksElapsed = last - start;
        assertEquals(102, ticksElapsed, 2);
        long realTimeElapsed = System.currentTimeMillis() - startReal;
        System.out.println("tickTimeElapsed " + ticksElapsed + " realTimeElapsed " + realTimeElapsed);
        assertTrue("tickTime should progress slower than real time", ticksElapsed < realTimeElapsed);
    }

    @Test
    public void testFasterTime() throws InterruptedException {
        long startReal = System.currentTimeMillis();
        long start = Time.tickTime();
        for (int i = 0; i < 100; i++) {
            Jvm.pause(1);
            // wait for System.currentTimeMillis() to change
            for (long ctm = System.currentTimeMillis(); ctm == System.currentTimeMillis(); )
                ;
            // these should all be called in the same ms
            for (int j = 0; j < 66; j++)
                Time.tickTime();
        }
        long last = Time.tickTime();
        long ticksElapsed = last - start;
        assertEquals(102, ticksElapsed, 2);
        long realTimeElapsed = System.currentTimeMillis() - startReal;
        System.out.println("tickTimeElapsed " + ticksElapsed + " realTimeElapsed " + realTimeElapsed);
        assertTrue("tickTime should progress slower than real time", ticksElapsed < realTimeElapsed);
    }
}