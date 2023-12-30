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

package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import static org.junit.Assert.*;

public class LongTimeTest extends CoreTestCommon {
    @Test
    public void secs() {
        long epoch = LongTime.EPOCH_SECS;
        assertTrue(LongTime.isSecs(epoch));
        assertTrue(LongTime.isSecs(LongTime.MAX_SECS));
        assertFalse(LongTime.isSecs(LongTime.EPOCH_MILLIS));
        assertTrue(LongTime.isSecs(LongTime.EPOCH_MILLIS / 1000));
        assertEquals(epoch, LongTime.toSecs(LongTime.toSecs(epoch)));
        assertEquals(epoch, LongTime.toSecs(LongTime.toMillis(epoch)));
        assertEquals(epoch, LongTime.toSecs(LongTime.toMicros(epoch)));
        assertEquals(epoch, LongTime.toSecs(LongTime.toNanos(epoch)));
    }

    @Test
    public void millis() {
        long epoch = LongTime.EPOCH_MILLIS;
        assertTrue(LongTime.isMillis(epoch));
        assertTrue(LongTime.isMillis(LongTime.MAX_MILLIS));
        assertFalse(LongTime.isMillis(LongTime.EPOCH_MICROS));
        assertTrue(LongTime.isMillis(LongTime.MAX_SECS * 1000));
        assertTrue(LongTime.isMillis(LongTime.EPOCH_MICROS / 1000));
        assertEquals(epoch - epoch % 1000, LongTime.toMillis(LongTime.toSecs(epoch)));
        assertEquals(epoch, LongTime.toMillis(LongTime.toMillis(epoch)));
        assertEquals(epoch, LongTime.toMillis(LongTime.toMicros(epoch)));
        assertEquals(epoch, LongTime.toMillis(LongTime.toNanos(epoch)));
    }

    @Test
    public void micros() {
        long epoch = LongTime.EPOCH_MICROS;
        assertTrue(LongTime.isMicros(epoch));
        assertTrue(LongTime.isMicros(LongTime.MAX_MICROS));
        assertFalse(LongTime.isMicros(LongTime.EPOCH_NANOS));
        assertTrue(LongTime.isMicros(LongTime.MAX_MILLIS * 1000));
        assertTrue(LongTime.isMicros(LongTime.EPOCH_NANOS / 1000));
        assertEquals(epoch - epoch % 1000000, LongTime.toMicros(LongTime.toSecs(epoch)));
        assertEquals(epoch - epoch % 1000, LongTime.toMicros(LongTime.toMillis(epoch)));
        assertEquals(epoch, LongTime.toMicros(LongTime.toMicros(epoch)));
        assertEquals(epoch, LongTime.toMicros(LongTime.toNanos(epoch)));
    }

    @Test
    public void nanos() {
        long epoch = LongTime.EPOCH_NANOS;
        assertTrue(LongTime.isNanos(epoch));
        assertTrue(LongTime.isNanos(LongTime.MAX_NANOS));
        assertTrue(LongTime.isNanos(LongTime.MAX_MICROS * 1000));
        assertEquals(epoch - epoch % 1000000000, LongTime.toNanos(LongTime.toSecs(epoch)));
        assertEquals(epoch - epoch % 1000000, LongTime.toNanos(LongTime.toMillis(epoch)));
        assertEquals(epoch - epoch % 1000, LongTime.toNanos(LongTime.toMicros(epoch)));
        assertEquals(epoch, LongTime.toNanos(LongTime.toNanos(epoch)));
    }
}
