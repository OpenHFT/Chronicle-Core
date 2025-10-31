/*
 * Copyright 2016-2025 chronicle.software
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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassMetricsTest {

    @Test
    public void constructorAndMethodsWorkCorrectly() {
        int expectedOffset = 10;
        int expectedLength = 20;
        ClassMetrics metrics = new ClassMetrics(expectedOffset, expectedLength);

        assertEquals(expectedOffset, metrics.offset());
        assertEquals(expectedLength, metrics.length());
    }

    @Test
    public void equalsAndHashCode() {
        ClassMetrics metrics1 = new ClassMetrics(10, 20);
        ClassMetrics metrics2 = new ClassMetrics(10, 20);
        ClassMetrics metrics3 = new ClassMetrics(15, 25);

        assertEquals(metrics1, metrics2);
        assertNotEquals(metrics1, metrics3);

        assertEquals(metrics1.hashCode(), metrics2.hashCode());
        assertNotEquals(metrics1.hashCode(), metrics3.hashCode());
    }

    @Test
    public void testToString() {
        ClassMetrics metrics = new ClassMetrics(10, 20);
        String toStringResult = metrics.toString();

        assertTrue(toStringResult.contains("offset=10"));
        assertTrue(toStringResult.contains("length=20"));
    }
}
