/*
 * Copyright 2016 higherfrequencytrading.com
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

/**
 * Created by peter on 10/07/15.
 */
public class TimeTest {

    @Test
    public void testTickTime() throws InterruptedException {
        long start = Time.tickTime();
        for (int i = 0; i < 10; i++) {
            Jvm.pause(10);
            Time.tickTime();
        }
        long last = Time.tickTime();
        assertEquals(11, last - start, 1);
    }

    @Test
    public void testFastTime() throws InterruptedException {
        long start = Time.tickTime();
        for (int i = 0; i < 100; i++) {
            Jvm.pause(1);
            Time.tickTime();
        }
        long last = Time.tickTime();
        assertEquals(102, last - start, 2);
    }
}